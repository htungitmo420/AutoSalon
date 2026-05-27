package org.example.orderservice.application.dto.request;

import java.util.Map;
import java.util.UUID;

public record CustomOrderRequest(
        UUID modelId,
        UUID customerId,
        Map<String, UUID> selectedPartIds
) {}
