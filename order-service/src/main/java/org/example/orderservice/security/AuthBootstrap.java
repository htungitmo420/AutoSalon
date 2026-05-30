package org.example.orderservice.security;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.repository.AuthUserRepository;
import org.example.orderservice.domain.auth.enums.AuthRole;
import org.example.orderservice.domain.auth.model.AuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthBootstrap {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.auth.bootstrap-admin.enabled:false}")
    private boolean enabled;

    @Value("${security.auth.bootstrap-admin.email:admin@autosalon.local}")
    private String email;

    @Value("${security.auth.bootstrap-admin.password:}")
    private String password;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void createAdminIfMissing() {
        if (!enabled) {
            return;
        }
        if (password.isBlank()) {
            throw new IllegalStateException(
                    "security.auth.bootstrap-admin.password is required when bootstrap admin is enabled");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (authUserRepository.findByEmail(normalizedEmail).isPresent()) {
            return;
        }

        AuthUser admin = AuthUser.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(password))
                .fullName("Local Admin")
                .enabled(true)
                .roles(Set.of(AuthRole.USER, AuthRole.MANAGER, AuthRole.WAREHOUSE_ADMIN, AuthRole.ADMIN))
                .build();
        authUserRepository.save(admin);
    }
}
