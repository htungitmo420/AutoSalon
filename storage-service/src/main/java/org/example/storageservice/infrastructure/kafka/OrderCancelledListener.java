package org.example.storageservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncontracts.event.OrderCancelledEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.storageservice.application.service.InventoryReservationService;
import org.example.storageservice.infrastructure.inbox.ProcessedEventService;
import org.example.storageservice.infrastructure.logging.TraceContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledListener {

    private final InventoryReservationService inventoryReservationService;
    private final ProcessedEventService processedEventService;

    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED_V1)
    public void handle(OrderCancelledEvent event) {
        TraceContext.runWithTraceId(event.traceId(), () -> {
            boolean processed = processedEventService.processOnce(
                    event.eventId(),
                    KafkaTopics.ORDER_CANCELLED_V1,
                    event.orderId().toString(),
                    event.traceId(),
                    () -> inventoryReservationService.releaseReservation(
                            event.orderId(), event.reservationId(), event.reason(), event.traceId()));
            if (!processed) {
                log.info("Skipped duplicate order cancelled event, eventId={}", event.eventId());
            }
        });
    }
}
