package org.example.orderservice.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.application.port.out.PasswordResetNotifier;
import org.example.orderservice.domain.auth.model.AuthUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "security.auth.password-reset.delivery-enabled",
        havingValue = "false",
        matchIfMissing = true)
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    @Override
    public void sendPasswordReset(AuthUser user, String rawToken) {
        String path = UriComponentsBuilder.fromPath("/reset-password")
                .queryParam("token", rawToken)
                .build()
                .toUriString();
        log.warn("Local-only password reset link for {}: {}", user.getEmail(), path);
    }
}
