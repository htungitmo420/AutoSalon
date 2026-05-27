package org.example.storageservice.application.port.out;

import org.example.storageservice.domain.reservation.model.InventoryReservation;

import java.util.UUID;

public interface InventoryReservationEventPublisher {

    void publishExpired(InventoryReservation reservation, String traceId);

    void publishAssemblyCompleted(InventoryReservation reservation, UUID assemblyOrderId, String traceId);

    void publishAssemblyFailed(InventoryReservation reservation, UUID assemblyOrderId, String reason, String traceId);
}
