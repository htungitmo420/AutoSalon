package org.example.storageservice.application.dto.request;

import org.example.storageservice.domain.car.enums.*;

import java.util.Set;
import java.util.UUID;

public record CarFilterRequest(
        Double minPrice,
        Double maxPrice,
        Brand brand,
        String modelName,
        BodyType bodyType,
        FuelType fuelType,
        Integer minEnginePower,
        Integer maxEnginePower,
        Double minEngineVolume,
        Double maxEngineVolume,
        GearBoxType gearBoxType,
        DrivetrainType drivetrainType,
        Color color,
        Set<UUID> componentIds
) {}