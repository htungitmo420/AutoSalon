package org.example.commoncontracts.event;

import org.example.commoncontracts.order.OrderType;

import java.time.Instant;
import java.util.UUID;

public record OrderRejectedEvent(
        UUID eventId,
        UUID orderId,
        OrderType orderType,
        String reason,
        String traceId,
        Instant occurredAt
) {}
