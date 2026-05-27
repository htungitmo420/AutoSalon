package org.example.storageservice.domain.stock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.storageservice.domain.BaseEntity;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "part_stocks", schema = "auto_salon")
public class PartStock extends BaseEntity {

    @Column(name = "part_id", nullable = false)
    private UUID partId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}
