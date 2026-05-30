package org.example.orderservice.application.service;

import org.example.orderservice.application.dto.request.LoginRequest;
import org.example.orderservice.application.dto.request.RegisterRequest;
import org.example.orderservice.application.exception.InvalidCredentialsException;
import org.example.orderservice.application.port.out.AuthRateLimiter;
import org.example.orderservice.application.port.out.JwtTokenIssuer;
import org.example.orderservice.application.repository.AuthUserRepository;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.infrastructure.inmemory.InMemoryAuthRefreshTokenRepository;
import org.example.orderservice.infrastructure.inmemory.InMemoryAuthUserRepository;
import org.example.orderservice.infrastructure.inmemory.InMemoryPasswordResetTokenRepository;
import org.example.orderservice.security.SecureOpaqueTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {

    private static final String VALID_PASSWORD = UUID.randomUUID() + Character.toString('!') + "Aa1";
    private static final String INVALID_PASSWORD = UUID.randomUUID().toString();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        AuthUserRepository repository = new InMemoryAuthUserRepository();
        JwtTokenIssuer tokenIssuer = new JwtTokenIssuer() {
            @Override
            public String issueToken(UUID userId, String email, Set<String> roles) {
                return "token-" + userId;
            }

            @Override
            public long expiresInSeconds() {
                return 3600;
            }
        };
        authService = new AuthService(
                repository,
                new InMemoryAuthRefreshTokenRepository(),
                new InMemoryPasswordResetTokenRepository(),
                new BCryptPasswordEncoder(),
                tokenIssuer,
                new SecureOpaqueTokenProvider(),
                (user, rawToken) -> {
                },
                new NoOpRateLimiter());
    }

    @Test
    void register_CreatesUserRoleAndReturnsToken() {
        var response = authService.register(new RegisterRequest(
                " USER@Example.COM ", VALID_PASSWORD, "Demo User"), "127.0.0.1");

        assertEquals("user@example.com", response.email());
        assertEquals("Demo User", response.fullName());
        assertEquals(List.of("USER"), response.roles());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600, response.expiresInSeconds());
        assertEquals(2592000, response.refreshTokenExpiresInSeconds());
    }

    @Test
    void register_DuplicateEmailRejected() {
        authService.register(new RegisterRequest("user@example.com", VALID_PASSWORD, "Demo User"), "127.0.0.1");

        assertThrows(DomainValidationException.class,
                () -> authService.register(
                        new RegisterRequest("USER@example.com", VALID_PASSWORD, "Demo User"),
                        "127.0.0.1"));
    }

    @Test
    void login_ValidCredentialsReturnToken() {
        authService.register(new RegisterRequest("user@example.com", VALID_PASSWORD, "Demo User"), "127.0.0.1");

        var response = authService.login(new LoginRequest("USER@example.com", VALID_PASSWORD), "127.0.0.1");

        assertEquals("user@example.com", response.email());
        assertEquals(List.of("USER"), response.roles());
    }

    @Test
    void login_InvalidCredentialsRejected() {
        authService.register(new RegisterRequest("user@example.com", VALID_PASSWORD, "Demo User"), "127.0.0.1");

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("user@example.com", INVALID_PASSWORD), "127.0.0.1"));
    }

    private static class NoOpRateLimiter implements AuthRateLimiter {

        @Override
        public void ensureAllowed(String action, String clientKey) {
        }

        @Override
        public void recordFailure(String action, String clientKey) {
        }

        @Override
        public void clear(String action, String clientKey) {
        }
    }
}
