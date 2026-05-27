package org.example.storageservice.application.dto.request;

import org.example.storageservice.domain.car.enums.Color;

import java.math.BigDecimal;
import java.util.UUID;

public record SaveCarRequest(
        UUID modelId,
        Color color,
        BigDecimal price
) {}