package org.example.storageservice.application.dto.response;

import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.car.enums.FuelType;

import java.math.BigDecimal;
import java.util.UUID;

public record AvailableCarResponse(
        UUID id,
        UUID modelId,
        String modelName,
        Brand brand,
        BodyType bodyType,
        FuelType fuelType,
        Color color,
        BigDecimal price
) {}
