package org.example.orderservice.application.dto.request;

public record RegisterRequest(
        String email,
        String password,
        String fullName
) {
}
