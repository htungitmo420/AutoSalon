package org.example.storageservice.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.service.InventoryReservationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "reservation.expiration.scheduling.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class InventoryReservationExpirationScheduler {

    private final InventoryReservationService inventoryReservationService;

    @Scheduled(fixedDelayString = "${reservation.expiration.fixed-delay-ms:30000}")
    public void expireHeldReservations() {
        inventoryReservationService.expireHeldReservations();
    }
}
