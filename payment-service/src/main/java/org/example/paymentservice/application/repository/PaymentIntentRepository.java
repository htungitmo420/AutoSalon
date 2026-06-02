package org.example.paymentservice.application.repository;

import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.domain.payment.PaymentIntent;
import org.example.paymentservice.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PaymentIntentRepository {
    PaymentIntent save(PaymentIntent paymentIntent);
    Optional<PaymentIntent> findById(UUID id);
    Optional<PaymentIntent> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentIntent> findByProviderPaymentIntentId(String providerPaymentIntentId);
    java.util.List<PaymentIntent> findByReferenceAndStatuses(
            UUID referenceId, PaymentTargetType targetType, Set<PaymentStatus> statuses);
    BigDecimal sumAmountByReferenceAndStatus(UUID referenceId, PaymentTargetType targetType, PaymentStatus status);
    boolean existsSucceededDeposit(UUID referenceId, PaymentTargetType targetType);
}
