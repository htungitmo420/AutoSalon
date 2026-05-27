package org.example.storageservice.application.dto.response;

import org.example.storageservice.domain.part.enums.PartType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record PartResponse(
        UUID id,
        PartType type,
        String name,
        BigDecimal surcharge,
        Set<UUID> compatibleModelIds
) {}