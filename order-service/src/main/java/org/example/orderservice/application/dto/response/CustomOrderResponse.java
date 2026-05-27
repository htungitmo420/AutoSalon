package org.example.orderservice.application.dto.response;

import org.example.orderservice.domain.order.enums.CustomOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CustomOrderResponse(
        UUID id,
        UUID modelId,
        UUID customerId,
        Map<String, UUID> selectedPartIds,
        BigDecimal totalPrice,
        CustomOrderStatus status,
        Instant createdAt
) {}