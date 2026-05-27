package org.example.storageservice.application.dto.request;

import org.example.storageservice.domain.part.enums.PartType;

import java.util.Map;
import java.util.UUID;

public record CarConfigurationRequest(
        Map<PartType, UUID> selectedPartIds
) {}