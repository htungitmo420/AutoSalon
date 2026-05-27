package org.example.storageservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.example.commoncontracts.event.AssemblyCompletedEvent;
import org.example.commoncontracts.event.AssemblyFailedEvent;
import org.example.commoncontracts.event.ReservationExpiredEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.commoncontracts.order.OrderType;
import org.example.storageservice.application.port.out.InventoryReservationEventPublisher;
import org.example.storageservice.domain.reservation.model.InventoryReservation;
import org.example.storageservice.infrastructure.outbox.OutboxService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaInventoryReservationEventPublisher implements InventoryReservationEventPublisher {

    private static final int EVENT_VERSION = 1;

    private final OutboxService outboxService;

    @Override
    public void publishExpired(InventoryReservation reservation, String traceId) {
        ReservationExpiredEvent event = new ReservationExpiredEvent(
                UUID.randomUUID(), EVENT_VERSION, reservation.getOrderId(),
                OrderType.valueOf(reservation.getSourceOrderType().name()), reservation.getId(), traceId,
                Instant.now());
        outboxService.save(KafkaTopics.RESERVATION_EXPIRED_V1, reservation.getOrderId().toString(), event);
    }

    @Override
    public void publishAssemblyCompleted(InventoryReservation reservation, UUID assemblyOrderId, String traceId) {
        AssemblyCompletedEvent event = new AssemblyCompletedEvent(
                UUID.randomUUID(), EVENT_VERSION, reservation.getOrderId(), reservation.getId(), assemblyOrderId,
                traceId, Instant.now());
        outboxService.save(KafkaTopics.ASSEMBLY_COMPLETED_V1, reservation.getOrderId().toString(), event);
    }

    @Override
    public void publishAssemblyFailed(InventoryReservation reservation, UUID assemblyOrderId, String reason,
                                      String traceId) {
        AssemblyFailedEvent event = new AssemblyFailedEvent(
                UUID.randomUUID(), EVENT_VERSION, reservation.getOrderId(), reservation.getId(), assemblyOrderId,
                reason, traceId, Instant.now());
        outboxService.save(KafkaTopics.ASSEMBLY_FAILED_V1, reservation.getOrderId().toString(), event);
    }
}
