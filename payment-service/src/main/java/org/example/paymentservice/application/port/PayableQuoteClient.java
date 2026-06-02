package org.example.paymentservice.application.port;

import org.example.commoncontracts.payment.PaymentTargetType;

import java.math.BigDecimal;
import java.util.UUID;

public interface PayableQuoteClient {
    PayableQuote getQuote(PaymentTargetType targetType, UUID referenceId, UUID customerId);

    record PayableQuote(PaymentTargetType targetType, UUID referenceId, UUID customerId,
                        BigDecimal totalAmount, boolean active) {
    }
}
