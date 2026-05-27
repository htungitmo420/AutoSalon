package org.example.storageservice.application.dto.response;

import java.util.UUID;

public record PartStockResponse(
        UUID id,
        UUID partId,
        int quantity,
        int reservedQuantity,
        int availableQuantity
) {}
