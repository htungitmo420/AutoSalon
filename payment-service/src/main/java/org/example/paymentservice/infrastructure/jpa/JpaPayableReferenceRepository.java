package org.example.paymentservice.infrastructure.jpa;

import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.domain.payment.PayableReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaPayableReferenceRepository extends JpaRepository<PayableReference, UUID> {
    Optional<PayableReference> findByTargetTypeAndReferenceId(PaymentTargetType targetType, UUID referenceId);
}
