package org.example.storageservice.application.dto.response;

import org.example.storageservice.domain.car.enums.*;
import org.example.storageservice.domain.part.enums.PartType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record CarModelResponse(
        UUID id,
        Brand brand,
        String modelName,
        BodyType bodyType,
        FuelType fuelType,
        int enginePower,
        double engineVolumeLiters,
        GearBoxType gearBoxType,
        DrivetrainType drivetrainType,
        BigDecimal basePrice,
        Map<PartType, UUID> basePartIds
) {}