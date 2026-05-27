package org.example.orderservice.domain.order.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.orderservice.domain.BaseEntity;
import org.example.orderservice.domain.order.enums.CommonOrderStatus;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "common_car_orders", schema = "auto_salon")
public class CommonCarOrder extends BaseEntity {

    @Column(name = "car_id", nullable = false)
    private UUID carId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommonOrderStatus status;
}
