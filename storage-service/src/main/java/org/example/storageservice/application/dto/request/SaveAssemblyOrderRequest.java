package org.example.storageservice.application.dto.request;

import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;

import java.util.Map;
import java.util.UUID;

public record SaveAssemblyOrderRequest(
        UUID sourceOrderId,
        SourceOrderType sourceOrderType,
        UUID carId,
        UUID modelId,
        Map<String, UUID> requiredPartIds,
        UUID warehouseEmployeeId,
        AssemblyOrderStatus status,
        String failureReason
) {}
