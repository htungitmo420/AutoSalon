package org.example.commoncontracts.event;

import org.example.commoncontracts.order.OrderType;

import java.time.Instant;
import java.util.UUID;

public record OrderApprovedEvent(
        UUID eventId,
        UUID orderId,
        OrderType orderType,
        String traceId,
        Instant occurredAt
) {}