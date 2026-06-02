package org.example.commoncontracts.event;

import org.example.commoncontracts.payment.PaymentPurpose;
import org.example.commoncontracts.payment.PaymentTargetType;

import java.time.Instant;
import java.util.UUID;

public record PaymentSucceededEvent(
        UUID eventId,
        int eventVersion,
        UUID paymentId,
        UUID referenceId,
        UUID customerId,
        PaymentTargetType targetType,
        PaymentPurpose purpose,
        String amount,
        String currency,
        String traceId,
        Instant occurredAt
) {
}
