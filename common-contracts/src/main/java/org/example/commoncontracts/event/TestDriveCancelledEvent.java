package org.example.commoncontracts.event;

import java.time.Instant;
import java.util.UUID;

public record TestDriveCancelledEvent(
        UUID eventId,
        int eventVersion,
        UUID testDriveId,
        UUID customerId,
        String traceId,
        Instant occurredAt
) {
}
