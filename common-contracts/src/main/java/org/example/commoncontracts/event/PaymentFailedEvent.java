package org.example.commoncontracts.event;

import org.example.commoncontracts.payment.PaymentPurpose;
import org.example.commoncontracts.payment.PaymentTargetType;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
        UUID eventId,
        int eventVersion,
        UUID paymentId,
        UUID referenceId,
        UUID customerId,
        PaymentTargetType targetType,
        PaymentPurpose purpose,
        String failureMessage,
        String traceId,
        Instant occurredAt
) {
}
