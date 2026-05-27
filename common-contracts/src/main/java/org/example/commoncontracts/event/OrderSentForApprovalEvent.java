package org.example.commoncontracts.event;

import org.example.commoncontracts.order.OrderType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record OrderSentForApprovalEvent(
        UUID eventId,
        UUID orderId,
        OrderType orderType,
        UUID carId,
        UUID modelId,
        Map<String, UUID> selectedPartIds,
        String traceId,
        Instant occurredAt
) {}
