package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.dto.request.LoginRequest;
import org.example.orderservice.application.dto.request.PasswordResetConfirmRequest;
import org.example.orderservice.application.dto.request.PasswordResetRequest;
import org.example.orderservice.application.dto.request.RefreshTokenRequest;
import org.example.orderservice.application.dto.request.RegisterRequest;
import org.example.orderservice.application.dto.response.AuthMessageResponse;
import org.example.orderservice.application.dto.response.AuthResponse;
import org.example.orderservice.application.dto.response.AuthenticatedUserResponse;
import org.example.orderservice.application.exception.InvalidAuthTokenException;
import org.example.orderservice.application.exception.InvalidCredentialsException;
import org.example.orderservice.application.mapper.AuthMapper;
import org.example.orderservice.application.port.out.AuthRateLimiter;
import org.example.orderservice.application.port.out.JwtTokenIssuer;
import org.example.orderservice.application.port.out.OpaqueTokenProvider;
import org.example.orderservice.application.port.out.PasswordResetNotifier;
import org.example.orderservice.application.repository.AuthRefreshTokenRepository;
import org.example.orderservice.application.repository.AuthUserRepository;
import org.example.orderservice.application.repository.PasswordResetTokenRepository;
import org.example.orderservice.domain.auth.model.AuthRefreshToken;
import org.example.orderservice.domain.auth.model.AuthUser;
import org.example.orderservice.domain.auth.model.PasswordResetToken;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[^A-Za-z0-9]");
    private static final String PASSWORD_RESET_REQUESTED = "If the account exists, a password reset link has been sent";

    private final AuthUserRepository authUserRepository;
    private final AuthRefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenIssuer jwtTokenIssuer;
    private final OpaqueTokenProvider opaqueTokenProvider;
    private final PasswordResetNotifier passwordResetNotifier;
    private final AuthRateLimiter authRateLimiter;

    @Value("${security.auth.refresh-token-ttl-seconds:2592000}")
    private long refreshTokenTtlSeconds = 2592000;

    @Value("${security.auth.password-reset.token-ttl-seconds:900}")
    private long passwordResetTokenTtlSeconds = 900;

    @Value("${security.auth.password.min-length:12}")
    private int minimumPasswordLength = 12;

    @Transactional
    public AuthResponse register(RegisterRequest request, String clientAddress) {
        requireRequest(request);
        String email = normalizeEmail(request.email());
        List<String> rateLimitKeys = rateLimitKeys(email, clientAddress);
        ensureRateLimitAllowed(AuthRateLimiter.REGISTER, rateLimitKeys);
        recordRateLimitFailure(AuthRateLimiter.REGISTER, rateLimitKeys);
        validateEmail(email);
        validatePassword(request.password());

        authUserRepository.findByEmail(email).ifPresent(existing -> {
            throw new DomainValidationException("Email is already registered");
        });

        AuthUser user = AuthMapper.INSTANCE.toRegisteredUser(email, passwordEncoder.encode(request.password()),
                normalizeFullName(request.fullName()));

        return issueSession(authUserRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientAddress) {
        requireRequest(request);
        String email = normalizeEmail(request.email());
        List<String> rateLimitKeys = rateLimitKeys(email, clientAddress);
        ensureRateLimitAllowed(AuthRateLimiter.LOGIN, rateLimitKeys);

        AuthUser user = authUserRepository.findByEmail(email).orElse(null);
        if (user == null || !user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            recordRateLimitFailure(AuthRateLimiter.LOGIN, rateLimitKeys);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        authRateLimiter.clear(AuthRateLimiter.LOGIN, emailRateLimitKey(email));
        return issueSession(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        requireRequest(request);
        String tokenHash = opaqueTokenProvider.hash(request.refreshToken());
        AuthRefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> invalidRefreshToken());
        Instant now = Instant.now();

        if (!currentToken.isActiveAt(now)) {
            refreshTokenRepository.revokeFamily(currentToken.getFamilyId(), now);
            throw invalidRefreshToken();
        }

        AuthUser user = requireEnabledUser(currentToken.getUserId());
        RefreshTokenPair nextToken = createRefreshToken(user.getId(), currentToken.getFamilyId(), now);
        currentToken.setRevokedAt(now);
        currentToken.setReplacedByTokenHash(nextToken.entity().getTokenHash());
        refreshTokenRepository.save(currentToken);
        refreshTokenRepository.save(nextToken.entity());
        return toAuthResponse(user, nextToken);
    }

    @Transactional
    public AuthMessageResponse logout(RefreshTokenRequest request) {
        requireRequest(request);
        String tokenHash = opaqueTokenProvider.hash(request.refreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> refreshTokenRepository.revokeFamily(token.getFamilyId(), Instant.now()));
        return new AuthMessageResponse("Session revoked");
    }

    @Transactional
    public AuthMessageResponse logoutAll(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
        return new AuthMessageResponse("All sessions revoked");
    }

    @Transactional
    public AuthMessageResponse requestPasswordReset(PasswordResetRequest request, String clientAddress) {
        requireRequest(request);
        String email = normalizeEmail(request.email());
        List<String> rateLimitKeys = rateLimitKeys(email, clientAddress);
        ensureRateLimitAllowed(AuthRateLimiter.PASSWORD_RESET, rateLimitKeys);
        recordRateLimitFailure(AuthRateLimiter.PASSWORD_RESET, rateLimitKeys);

        authUserRepository.findByEmail(email)
                .filter(AuthUser::isEnabled)
                .ifPresent(this::issuePasswordResetToken);
        return new AuthMessageResponse(PASSWORD_RESET_REQUESTED);
    }

    @Transactional
    public AuthMessageResponse resetPassword(PasswordResetConfirmRequest request) {
        requireRequest(request);
        validatePassword(request.newPassword());

        String tokenHash = opaqueTokenProvider.hash(request.token());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> invalidPasswordResetToken());
        Instant now = Instant.now();
        if (!resetToken.canBeConsumedAt(now)) {
            throw invalidPasswordResetToken();
        }

        AuthUser user = requireEnabledUser(resetToken.getUserId());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        authUserRepository.save(user);
        passwordResetTokenRepository.invalidateAllByUserId(user.getId(), now);
        refreshTokenRepository.revokeAllByUserId(user.getId(), now);
        return new AuthMessageResponse("Password updated. Sign in again");
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse currentUser(UUID userId) {
        AuthUser user = requireEnabledUser(userId);
        return new AuthenticatedUserResponse(user.getId(), user.getEmail(), user.getFullName(), roles(user));
    }

    private AuthResponse issueSession(AuthUser user) {
        RefreshTokenPair refreshToken = createRefreshToken(user.getId(), UUID.randomUUID(), Instant.now());
        refreshTokenRepository.save(refreshToken.entity());
        return toAuthResponse(user, refreshToken);
    }

    private AuthResponse toAuthResponse(AuthUser user, RefreshTokenPair refreshToken) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        String accessToken = jwtTokenIssuer.issueToken(user.getId(), user.getEmail(), roleNames);
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                roles(user),
                accessToken,
                "Bearer",
                jwtTokenIssuer.expiresInSeconds(),
                refreshToken.rawValue(),
                refreshTokenTtlSeconds);
    }

    private RefreshTokenPair createRefreshToken(UUID userId, UUID familyId, Instant now) {
        String rawToken = opaqueTokenProvider.generate();
        AuthRefreshToken entity = AuthRefreshToken.builder()
                .userId(userId)
                .familyId(familyId)
                .tokenHash(opaqueTokenProvider.hash(rawToken))
                .expiresAt(now.plusSeconds(refreshTokenTtlSeconds))
                .build();
        return new RefreshTokenPair(rawToken, entity);
    }

    private void issuePasswordResetToken(AuthUser user) {
        Instant now = Instant.now();
        passwordResetTokenRepository.invalidateAllByUserId(user.getId(), now);
        String rawToken = opaqueTokenProvider.generate();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(opaqueTokenProvider.hash(rawToken))
                .expiresAt(now.plusSeconds(passwordResetTokenTtlSeconds))
                .build();
        passwordResetTokenRepository.save(resetToken);
        passwordResetNotifier.sendPasswordReset(user, rawToken);
    }

    private AuthUser requireEnabledUser(UUID userId) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + userId));
        if (!user.isEnabled()) {
            throw new InvalidAuthTokenException("Account is disabled");
        }
        return user;
    }

    private List<String> roles(AuthUser user) {
        return user.getRoles().stream()
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeFullName(String fullName) {
        return fullName == null || fullName.isBlank() ? null : fullName.trim();
    }

    private List<String> rateLimitKeys(String email, String clientAddress) {
        return List.of(emailRateLimitKey(email), "ip:" + normalizeClientAddress(clientAddress));
    }

    private String emailRateLimitKey(String email) {
        return "email:" + email;
    }

    private String normalizeClientAddress(String clientAddress) {
        return clientAddress == null || clientAddress.isBlank() ? "unknown" : clientAddress;
    }

    private void ensureRateLimitAllowed(String action, List<String> rateLimitKeys) {
        rateLimitKeys.forEach(key -> authRateLimiter.ensureAllowed(action, key));
    }

    private void recordRateLimitFailure(String action, List<String> rateLimitKeys) {
        rateLimitKeys.forEach(key -> authRateLimiter.recordFailure(action, key));
    }

    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new DomainValidationException("Valid email is required");
        }
    }

    private void validatePassword(String password) {
        if (password == null
                || password.length() < minimumPasswordLength
                || !UPPERCASE_PATTERN.matcher(password).find()
                || !LOWERCASE_PATTERN.matcher(password).find()
                || !DIGIT_PATTERN.matcher(password).find()
                || !SPECIAL_PATTERN.matcher(password).find()) {
            throw new DomainValidationException(
                    "Password must contain at least " + minimumPasswordLength
                            + " characters, uppercase, lowercase, number and special character");
        }
    }

    private void requireRequest(Object request) {
        if (request == null) {
            throw new DomainValidationException("Request body is required");
        }
    }

    private InvalidAuthTokenException invalidRefreshToken() {
        return new InvalidAuthTokenException("Invalid or expired refresh token");
    }

    private InvalidAuthTokenException invalidPasswordResetToken() {
        return new InvalidAuthTokenException("Invalid or expired password reset token");
    }

    private record RefreshTokenPair(String rawValue, AuthRefreshToken entity) {
    }
}
