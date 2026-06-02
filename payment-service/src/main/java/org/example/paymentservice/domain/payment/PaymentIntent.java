package org.example.paymentservice.domain.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.commoncontracts.payment.PaymentPurpose;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_intents", schema = "auto_salon")
public class PaymentIntent extends BaseEntity {

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private PaymentTargetType targetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentPurpose purpose;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "provider_payment_intent_id", unique = true)
    private String providerPaymentIntentId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "failure_message")
    private String failureMessage;
}
