package org.example.orderservice.infrastructure.grpc;

import org.example.commoncontracts.grpc.cart.CartCommandRequest;
import org.example.commoncontracts.grpc.cart.LockCartRequest;
import org.example.orderservice.application.dto.response.CartSnapshotResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CartSnapshotGrpcMapper {

    private CartSnapshotGrpcMapper() {
    }

    public static LockCartRequest toLockRequest(UUID cartId, UUID customerId, String traceId) {
        return LockCartRequest.newBuilder()
                .setCartId(cartId.toString())
                .setCustomerId(customerId.toString())
                .setTraceId(traceId)
                .build();
    }

    public static CartCommandRequest toCommandRequest(UUID cartId, UUID customerId, String traceId) {
        return CartCommandRequest.newBuilder()
                .setCartId(cartId.toString())
                .setCustomerId(customerId.toString())
                .setTraceId(traceId)
                .build();
    }

    public static CartSnapshotResponse toResponse(
            org.example.commoncontracts.grpc.cart.CartSnapshotResponse response) {
        Map<String, UUID> selectedPartIds = response.getSelectedPartIdsMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> UUID.fromString(entry.getValue())));
        return new CartSnapshotResponse(
                UUID.fromString(response.getCartId()),
                UUID.fromString(response.getCustomerId()),
                response.getStatus(),
                response.getItemType(),
                uuidOrNull(response.getCarId()),
                uuidOrNull(response.getModelId()),
                selectedPartIds,
                new BigDecimal(response.getQuotedPrice()),
                Instant.parse(response.getQuoteExpiresAt()),
                response.getVersion());
    }

    private static UUID uuidOrNull(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }
}
