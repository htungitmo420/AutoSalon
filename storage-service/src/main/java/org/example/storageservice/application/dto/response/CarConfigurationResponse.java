package org.example.storageservice.application.dto.response;

import org.example.storageservice.domain.part.enums.PartType;

import java.math.BigDecimal;
import java.util.Map;

public record CarConfigurationResponse(
        CarModelResponse carModel,
        Map<PartType, PartResponse> selectedParts,
        BigDecimal totalPrice
) {}