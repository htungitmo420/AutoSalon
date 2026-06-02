package org.example.orderservice.application.client;

import org.example.orderservice.application.dto.response.InventoryReservationResponse;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface InventoryReservationClient {

    InventoryReservationResponse reserveStockCar(UUID orderId, UUID carId, Instant expiresAt);

    InventoryReservationResponse reserveConfiguration(UUID orderId, UUID modelId, Map<String, UUID> selectedPartIds,
                                                      Instant expiresAt);

    InventoryReservationResponse confirmReservation(UUID orderId, UUID reservationId);

    InventoryReservationResponse releaseReservation(UUID orderId, UUID reservationId, String reason);
}
