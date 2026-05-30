package org.example.orderservice.application.dto.request;

public record PasswordResetConfirmRequest(
        String token,
        String newPassword
) {
}
