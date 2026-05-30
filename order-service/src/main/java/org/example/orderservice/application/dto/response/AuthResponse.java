package org.example.orderservice.application.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String fullName,
        List<String> roles,
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        @JsonIgnore
        String refreshToken,
        long refreshTokenExpiresInSeconds
) {
}
