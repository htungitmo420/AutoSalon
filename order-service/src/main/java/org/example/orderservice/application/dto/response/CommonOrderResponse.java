package org.example.orderservice.application.dto.response;

import org.example.orderservice.domain.order.enums.CommonOrderStatus;

import java.time.Instant;
import java.util.UUID;

public record CommonOrderResponse(
        UUID id,
        UUID carId,
        UUID customerId,
        CommonOrderStatus status,
        Instant createdAt
) {}