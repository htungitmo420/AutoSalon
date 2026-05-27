package org.example.storageservice.domain.car.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.storageservice.domain.BaseEntity;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.part.enums.PartType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "car_models", schema = "auto_salon")
public class CarModel extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Brand brand;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_type", nullable = false)
    private BodyType bodyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private FuelType fuelType;

    @Column(name = "engine_power", nullable = false)
    private int enginePower;

    @Column(name = "engine_volume_liters", nullable = false)
    private double engineVolumeLiters;

    @Enumerated(EnumType.STRING)
    @Column(name = "gear_box_type", nullable = false)
    private GearBoxType gearBoxType;

    @Enumerated(EnumType.STRING)
    @Column(name = "drivetrain_type", nullable = false)
    private DrivetrainType drivetrainType;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @ElementCollection
    @CollectionTable(name = "car_model_base_parts", joinColumns = @JoinColumn(name = "car_model_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "part_type")
    @Column(name = "part_id", nullable = false)
    @Builder.Default
    private Map<PartType, UUID> basePartIds = new HashMap<>();
}