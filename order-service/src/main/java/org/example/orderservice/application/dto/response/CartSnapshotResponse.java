package org.example.orderservice.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CartSnapshotResponse(
        UUID cartId,
        UUID customerId,
        String status,
        String itemType,
        UUID carId,
        UUID modelId,
        Map<String, UUID> selectedPartIds,
        BigDecimal quotedPrice,
        Instant quoteExpiresAt,
        long version
) {
}
