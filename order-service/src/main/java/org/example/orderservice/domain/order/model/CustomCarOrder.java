package org.example.orderservice.domain.order.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.orderservice.domain.BaseEntity;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "custom_car_orders", schema = "auto_salon")
public class CustomCarOrder extends BaseEntity {

    @Column(name = "model_id", nullable = false)
    private UUID modelId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @ElementCollection
    @CollectionTable(name = "custom_order_selected_parts", joinColumns = @JoinColumn(name = "custom_order_id"))
    @MapKeyColumn(name = "part_type")
    @Column(name = "part_id", nullable = false)
    @Builder.Default
    private Map<String, UUID> selectedPartIds = new HashMap<>();

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "cart_id")
    private UUID cartId;

    @Column(name = "paid_amount", nullable = false)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomOrderStatus status;
}
