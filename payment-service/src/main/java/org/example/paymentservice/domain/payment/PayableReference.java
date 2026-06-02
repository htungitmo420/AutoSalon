package org.example.paymentservice.domain.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Table(name = "payable_references", schema = "auto_salon",
        uniqueConstraints = @UniqueConstraint(name = "uq_payable_reference", columnNames = {"target_type", "reference_id"}))
public class PayableReference extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private PaymentTargetType targetType;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
