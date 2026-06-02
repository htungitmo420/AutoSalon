package org.example.paymentservice.application.dto;

import org.example.commoncontracts.payment.PaymentPurpose;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentIntentResponse(
        UUID id,
        UUID referenceId,
        PaymentTargetType targetType,
        PaymentPurpose purpose,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String clientSecret
) {
}
