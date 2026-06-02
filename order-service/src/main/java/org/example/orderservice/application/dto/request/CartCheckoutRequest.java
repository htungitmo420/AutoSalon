package org.example.orderservice.application.dto.request;

import java.util.UUID;

public record CartCheckoutRequest(
        UUID cartId
) {
}
