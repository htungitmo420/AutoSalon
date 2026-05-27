package org.example.commoncontracts.event;

import java.time.Instant;
import java.util.UUID;

public record AssemblyFailedEvent(
        UUID eventId,
        int eventVersion,
        UUID orderId,
        UUID reservationId,
        UUID assemblyOrderId,
        String reason,
        String traceId,
        Instant occurredAt
) {
}
