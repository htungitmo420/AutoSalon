package org.example.paymentservice.application.dto;

import org.example.commoncontracts.payment.PaymentPurpose;
import org.example.commoncontracts.payment.PaymentTargetType;

import java.util.UUID;

public record CreatePaymentIntentRequest(
        PaymentTargetType targetType,
        UUID referenceId,
        PaymentPurpose purpose
) {
}
