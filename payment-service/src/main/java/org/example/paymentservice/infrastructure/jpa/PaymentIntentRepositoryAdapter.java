package org.example.paymentservice.infrastructure.jpa;

import lombok.RequiredArgsConstructor;
import org.example.commoncontracts.payment.PaymentPurpose;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.application.repository.PaymentIntentRepository;
import org.example.paymentservice.domain.payment.PaymentIntent;
import org.example.paymentservice.domain.payment.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentIntentRepositoryAdapter implements PaymentIntentRepository {

    private final JpaPaymentIntentRepository delegate;

    public PaymentIntent save(PaymentIntent paymentIntent) {
        return delegate.save(paymentIntent);
    }

    public Optional<PaymentIntent> findById(UUID id) {
        return delegate.findById(id);
    }

    public Optional<PaymentIntent> findByIdempotencyKey(String idempotencyKey) {
        return delegate.findByIdempotencyKey(idempotencyKey);
    }

    public Optional<PaymentIntent> findByProviderPaymentIntentId(String providerPaymentIntentId) {
        return delegate.findByProviderPaymentIntentId(providerPaymentIntentId);
    }

    public List<PaymentIntent> findByReferenceAndStatuses(
            UUID referenceId, PaymentTargetType targetType, Set<PaymentStatus> statuses) {
        return delegate.findAllByReferenceIdAndTargetTypeAndStatusIn(referenceId, targetType, statuses);
    }

    public BigDecimal sumAmountByReferenceAndStatus(UUID referenceId, PaymentTargetType targetType,
                                                    PaymentStatus status) {
        return delegate.sumAmount(referenceId, targetType, status);
    }

    public boolean existsSucceededDeposit(UUID referenceId, PaymentTargetType targetType) {
        return delegate.existsByReferenceIdAndTargetTypeAndPurposeAndStatus(
                referenceId, targetType, PaymentPurpose.CAR_DEPOSIT, PaymentStatus.SUCCEEDED);
    }
}
