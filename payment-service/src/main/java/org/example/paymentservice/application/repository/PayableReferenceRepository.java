package org.example.paymentservice.application.repository;

import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.domain.payment.PayableReference;

import java.util.Optional;
import java.util.UUID;

public interface PayableReferenceRepository {
    PayableReference save(PayableReference payableReference);
    Optional<PayableReference> find(PaymentTargetType targetType, UUID referenceId);
}
