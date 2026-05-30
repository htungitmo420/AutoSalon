package org.example.storageservice.application.dto.response;

import org.example.storageservice.domain.assembly.enums.SourceOrderType;
import org.example.storageservice.domain.reservation.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record InventoryReservationResponse(
        UUID id,
        UUID orderId,
        SourceOrderType sourceOrderType,
        UUID carId,
        UUID modelId,
        Map<String, UUID> requiredPartIds,
        BigDecimal totalPrice,
        ReservationStatus status,
        Instant expiresAt,
        Instant confirmedAt,
        String releaseReason,
        Instant createdAt,
        Instant updatedAt
) {}
