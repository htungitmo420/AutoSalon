package org.example.orderservice.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AvailableCarResponse(
        UUID id,
        UUID modelId,
        String modelName,
        String brand,
        String bodyType,
        String fuelType,
        String color,
        BigDecimal price
) {}
