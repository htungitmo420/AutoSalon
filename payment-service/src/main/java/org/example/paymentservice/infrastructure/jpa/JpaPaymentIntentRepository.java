package org.example.paymentservice.infrastructure.jpa;

import org.example.commoncontracts.payment.PaymentPurpose;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.domain.payment.PaymentIntent;
import org.example.paymentservice.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.UUID;

public interface JpaPaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {
    Optional<PaymentIntent> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentIntent> findByProviderPaymentIntentId(String providerPaymentIntentId);
    List<PaymentIntent> findAllByReferenceIdAndTargetTypeAndStatusIn(
            UUID referenceId, PaymentTargetType targetType, Set<PaymentStatus> statuses);

    @Query("""
            select coalesce(sum(p.amount), 0) from PaymentIntent p
            where p.referenceId = :referenceId and p.targetType = :targetType and p.status = :status
            """)
    BigDecimal sumAmount(UUID referenceId, PaymentTargetType targetType, PaymentStatus status);

    boolean existsByReferenceIdAndTargetTypeAndPurposeAndStatus(
            UUID referenceId, PaymentTargetType targetType, PaymentPurpose purpose, PaymentStatus status);
}
