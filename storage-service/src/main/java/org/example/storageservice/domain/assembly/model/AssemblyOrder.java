package org.example.storageservice.domain.assembly.model;

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
import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assembly_orders", schema = "auto_salon")
public class AssemblyOrder extends BaseEntity {

    @Column(name = "source_order_id", nullable = false)
    private UUID sourceOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_order_type", nullable = false)
    private SourceOrderType sourceOrderType;

    @Column(name = "car_id")
    private UUID carId;

    @Column(name = "model_id")
    private UUID modelId;

    @ElementCollection
    @CollectionTable(
            name = "assembly_order_required_parts",
            schema = "auto_salon",
            joinColumns = @JoinColumn(name = "assembly_order_id")
    )
    @MapKeyColumn(name = "part_slot")
    @Column(name = "part_id", nullable = false)
    @Builder.Default
    private Map<String, UUID> requiredPartIds = new HashMap<>();

    @Column(name = "warehouse_employee_id")
    private UUID warehouseEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssemblyOrderStatus status;

    @Column(name = "failure_reason")
    private String failureReason;
}
