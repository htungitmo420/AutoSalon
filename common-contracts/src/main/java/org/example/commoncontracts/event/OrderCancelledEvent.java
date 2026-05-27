package org.example.commoncontracts.event;

import org.example.commoncontracts.order.OrderType;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID eventId,
        int eventVersion,
        UUID orderId,
        OrderType orderType,
        UUID reservationId,
        String reason,
        String traceId,
        Instant occurredAt
) {
}
