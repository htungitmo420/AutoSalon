package org.example.commoncontracts.event;

import java.time.Instant;
import java.util.UUID;

public record AssemblyCompletedEvent(
        UUID eventId,
        int eventVersion,
        UUID orderId,
        UUID reservationId,
        UUID assemblyOrderId,
        String traceId,
        Instant occurredAt
) {
}
