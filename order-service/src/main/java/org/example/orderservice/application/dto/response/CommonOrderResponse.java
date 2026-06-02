package org.example.orderservice.application.dto.response;

import org.example.orderservice.domain.order.enums.CommonOrderStatus;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

public record CommonOrderResponse(
        UUID id,
        UUID carId,
        UUID customerId,
        BigDecimal totalPrice,
        BigDecimal paidAmount,
        CommonOrderStatus status,
        Instant createdAt
) {}
