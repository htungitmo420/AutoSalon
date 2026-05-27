package org.example.storageservice.domain.reservation.model;

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
import org.example.storageservice.domain.BaseEntity;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;
import org.example.storageservice.domain.reservation.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventory_reservations", schema = "auto_salon")
public class InventoryReservation extends BaseEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_order_type", nullable = false)
    private SourceOrderType sourceOrderType;

    @Column(name = "car_id")
    private UUID carId;

    @Column(name = "model_id")
    private UUID modelId;

    @ElementCollection
    @CollectionTable(
            name = "reservation_required_parts",
            schema = "auto_salon",
            joinColumns = @JoinColumn(name = "reservation_id"))
    @MapKeyColumn(name = "part_slot")
    @Column(name = "part_id", nullable = false)
    @Builder.Default
    private Map<String, UUID> requiredPartIds = new HashMap<>();

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "release_reason")
    private String releaseReason;
}
