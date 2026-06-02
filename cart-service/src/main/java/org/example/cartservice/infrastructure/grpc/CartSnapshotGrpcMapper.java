package org.example.cartservice.infrastructure.grpc;

import org.example.cartservice.domain.cart.model.Cart;
import org.example.commoncontracts.grpc.cart.CartSnapshotResponse;

import java.util.Map;
import java.util.stream.Collectors;

public final class CartSnapshotGrpcMapper {

    private CartSnapshotGrpcMapper() {
    }

    public static CartSnapshotResponse toResponse(Cart cart) {
        Map<String, String> selectedPartIds = cart.getSelectedPartIds().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
        CartSnapshotResponse.Builder response = CartSnapshotResponse.newBuilder()
                .setCartId(cart.getId().toString())
                .setCustomerId(cart.getCustomerId().toString())
                .setStatus(cart.getStatus().name())
                .setItemType(cart.getItemType().name())
                .putAllSelectedPartIds(selectedPartIds)
                .setQuotedPrice(cart.getQuotedPrice().toPlainString())
                .setQuoteExpiresAt(cart.getQuoteExpiresAt().toString())
                .setVersion(cart.getVersion());
        if (cart.getCarId() != null) {
            response.setCarId(cart.getCarId().toString());
        }
        if (cart.getModelId() != null) {
            response.setModelId(cart.getModelId().toString());
        }
        return response.build();
    }
}
