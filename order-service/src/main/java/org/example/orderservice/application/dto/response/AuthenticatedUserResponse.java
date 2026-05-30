package org.example.orderservice.application.dto.response;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUserResponse(
        UUID userId,
        String email,
        String fullName,
        List<String> roles
) {
}
