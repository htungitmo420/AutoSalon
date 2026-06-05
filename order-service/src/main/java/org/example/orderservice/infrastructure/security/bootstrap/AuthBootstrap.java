package org.example.orderservice.infrastructure.security.bootstrap;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.repository.AuthUserRepository;
import org.example.orderservice.domain.auth.model.AuthUser;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AuthBootstrap {

    private static final String MISSING_PASSWORD_MESSAGE =
            "security.auth.bootstrap.default-password or users[].password is required when bootstrap is enabled";

    private final AuthBootstrapProperties properties;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void createConfiguredUsers() {
        if (!properties.isEnabled()) {
            return;
        }

        properties.getUsers().forEach(this::createOrUpdateUser);
    }

    private void createOrUpdateUser(AuthBootstrapProperties.BootstrapUser user) {
        validate(user);

        String normalizedEmail = normalizeEmail(user.getEmail());
        authUserRepository.findByEmail(normalizedEmail).ifPresentOrElse(
                existingUser -> updateUserIfConfigured(existingUser, user),
                () -> createUser(normalizedEmail, user));
    }

    private void validate(AuthBootstrapProperties.BootstrapUser user) {
        if (user.getEmail().isBlank()) {
            throw new IllegalStateException("security.auth.bootstrap.users[].email must not be blank");
        }
        if (effectivePassword(user).isBlank()) {
            throw new IllegalStateException(MISSING_PASSWORD_MESSAGE);
        }
        if (user.getRoles().isEmpty()) {
            throw new IllegalStateException("security.auth.bootstrap.users[].roles must not be empty");
        }
    }

    private void updateUserIfConfigured(AuthUser existingUser, AuthBootstrapProperties.BootstrapUser user) {
        if (!properties.isUpdateExistingUsers()) {
            return;
        }

        existingUser.setPasswordHash(passwordEncoder.encode(effectivePassword(user)));
        existingUser.setFullName(effectiveFullName(user));
        existingUser.setEnabled(true);
        existingUser.setRoles(new HashSet<>(user.getRoles()));
        authUserRepository.save(existingUser);
    }

    private void createUser(String normalizedEmail, AuthBootstrapProperties.BootstrapUser user) {
        AuthUser authUser = AuthUser.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(effectivePassword(user)))
                .fullName(effectiveFullName(user))
                .enabled(true)
                .roles(new HashSet<>(user.getRoles()))
                .build();
        authUserRepository.save(authUser);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String effectivePassword(AuthBootstrapProperties.BootstrapUser user) {
        return user.getPassword().isBlank() ? properties.getDefaultPassword() : user.getPassword();
    }

    private String effectiveFullName(AuthBootstrapProperties.BootstrapUser user) {
        return user.getFullName().isBlank() ? normalizeEmail(user.getEmail()) : user.getFullName().trim();
    }
}
