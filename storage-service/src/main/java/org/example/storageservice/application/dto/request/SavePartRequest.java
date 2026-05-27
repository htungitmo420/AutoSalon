package org.example.storageservice.application.dto.request;

import org.example.storageservice.domain.part.enums.PartType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record SavePartRequest(
        String name,
        PartType type,
        BigDecimal surcharge,
        Set<UUID> compatibleModelIds
) {}