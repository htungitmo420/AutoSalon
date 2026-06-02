package org.example.cartservice.application.dto.response;

import org.example.cartservice.domain.cart.enums.CartItemType;
import org.example.cartservice.domain.cart.enums.CartStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CartResponse(
        UUID id,
        UUID customerId,
        CartStatus status,
        CartItemType itemType,
        UUID carId,
        UUID modelId,
        Map<String, UUID> selectedPartIds,
        BigDecimal quotedPrice,
        Instant quoteExpiresAt,
        long version
) {
}
