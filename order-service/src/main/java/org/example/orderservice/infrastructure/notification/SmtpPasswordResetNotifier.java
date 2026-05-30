package org.example.orderservice.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.port.out.PasswordResetNotifier;
import org.example.orderservice.domain.auth.model.AuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.password-reset.delivery-enabled", havingValue = "true")
public class SmtpPasswordResetNotifier implements PasswordResetNotifier {

    private final JavaMailSender mailSender;

    @Value("${security.auth.password-reset.frontend-url}")
    private String frontendUrl;

    @Value("${security.auth.password-reset.mail-from}")
    private String mailFrom;

    @Override
    public void sendPasswordReset(AuthUser user, String rawToken) {
        String resetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("Reset your AutoSalon password");
        message.setText("Open this link to reset your password: " + resetUrl);
        mailSender.send(message);
    }
}
