package org.example.orderservice.application.dto.request;

import java.util.UUID;

public record CommonOrderRequest(
        UUID carId,
        UUID customerId
) {}