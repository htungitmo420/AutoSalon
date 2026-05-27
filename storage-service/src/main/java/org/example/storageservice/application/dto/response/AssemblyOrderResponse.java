package org.example.storageservice.application.dto.response;

import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AssemblyOrderResponse(
        UUID id,
        UUID sourceOrderId,
        SourceOrderType sourceOrderType,
        UUID carId,
        UUID modelId,
        Map<String, UUID> requiredPartIds,
        UUID warehouseEmployeeId,
        AssemblyOrderStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {}
