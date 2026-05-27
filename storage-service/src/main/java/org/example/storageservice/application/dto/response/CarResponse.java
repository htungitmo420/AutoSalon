package org.example.storageservice.application.dto.response;

import org.example.storageservice.domain.car.enums.Color;

import java.math.BigDecimal;
import java.util.UUID;

public record CarResponse(
        UUID id,
        UUID modelId,
        Color color,
        BigDecimal price,
        boolean testDrive
) {}