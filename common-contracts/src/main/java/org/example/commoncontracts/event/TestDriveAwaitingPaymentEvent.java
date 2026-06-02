package org.example.commoncontracts.event;

import java.time.Instant;
import java.util.UUID;

public record TestDriveAwaitingPaymentEvent(
        UUID eventId,
        int eventVersion,
        UUID testDriveId,
        UUID customerId,
        String amount,
        String traceId,
        Instant occurredAt
) {
}
