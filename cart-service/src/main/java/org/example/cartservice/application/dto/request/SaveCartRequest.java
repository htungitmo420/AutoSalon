package org.example.cartservice.application.dto.request;

import org.example.cartservice.domain.cart.enums.CartItemType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SaveCartRequest(
        CartItemType itemType,
        UUID carId,
        UUID modelId,
        Map<String, UUID> selectedPartIds,
        BigDecimal quotedPrice,
        Instant quoteExpiresAt
) {
}
