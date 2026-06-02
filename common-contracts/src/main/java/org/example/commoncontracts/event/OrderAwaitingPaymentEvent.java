package org.example.commoncontracts.event;

import org.example.commoncontracts.order.OrderType;

import java.time.Instant;
import java.util.UUID;

public record OrderAwaitingPaymentEvent(
        UUID eventId,
        int eventVersion,
        UUID orderId,
        UUID customerId,
        OrderType orderType,
        UUID reservationId,
        String totalPrice,
        String traceId,
        Instant occurredAt
) {
}
