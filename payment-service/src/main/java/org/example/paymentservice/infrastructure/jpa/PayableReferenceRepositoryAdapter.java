package org.example.paymentservice.infrastructure.jpa;

import lombok.RequiredArgsConstructor;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.application.repository.PayableReferenceRepository;
import org.example.paymentservice.domain.payment.PayableReference;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PayableReferenceRepositoryAdapter implements PayableReferenceRepository {

    private final JpaPayableReferenceRepository delegate;

    public PayableReference save(PayableReference payableReference) {
        return delegate.save(payableReference);
    }

    public Optional<PayableReference> find(PaymentTargetType targetType, UUID referenceId) {
        return delegate.findByTargetTypeAndReferenceId(targetType, referenceId);
    }
}
