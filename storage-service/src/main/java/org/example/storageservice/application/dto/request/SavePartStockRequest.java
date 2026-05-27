package org.example.storageservice.application.dto.request;

import java.util.UUID;

public record SavePartStockRequest(
        UUID partId,
        int quantity,
        int reservedQuantity
) {}
