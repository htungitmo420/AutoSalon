package org.example.orderservice.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryReservationResponse(
        UUID reservationId,
        String status,
        Instant expiresAt,
        BigDecimal totalPrice
) {
}
