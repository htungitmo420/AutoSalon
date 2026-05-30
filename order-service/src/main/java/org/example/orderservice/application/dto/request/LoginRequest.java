package org.example.orderservice.application.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}
