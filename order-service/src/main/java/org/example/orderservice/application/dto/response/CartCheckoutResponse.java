package org.example.orderservice.application.dto.response;

import java.util.UUID;

public record CartCheckoutResponse(
        UUID cartId,
        String itemType,
        CommonOrderResponse commonOrder,
        CustomOrderResponse customOrder
) {

    public static CartCheckoutResponse stockCar(UUID cartId, CommonOrderResponse order) {
        return new CartCheckoutResponse(cartId, "STOCK_CAR", order, null);
    }

    public static CartCheckoutResponse configuredCar(UUID cartId, CustomOrderResponse order) {
        return new CartCheckoutResponse(cartId, "CONFIGURED_CAR", null, order);
    }
}
