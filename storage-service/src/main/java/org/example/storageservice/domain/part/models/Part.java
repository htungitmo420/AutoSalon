package org.example.storageservice.domain.part.models;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.storageservice.domain.BaseEntity;
import org.example.storageservice.domain.part.enums.PartType;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "parts", schema = "auto_salon")
public class Part extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal surcharge;

    @ElementCollection
    @CollectionTable(name = "part_compatible_models", joinColumns = @JoinColumn(name = "part_id"))
    @Column(name = "model_id", nullable = false)
    @Builder.Default
    private Set<UUID> compatibleModelIds = new HashSet<>();
}