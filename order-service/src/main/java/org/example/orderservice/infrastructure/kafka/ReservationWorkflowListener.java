package org.example.orderservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncontracts.event.AssemblyCompletedEvent;
import org.example.commoncontracts.event.AssemblyFailedEvent;
import org.example.commoncontracts.event.ReservationExpiredEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.commoncontracts.order.OrderType;
import org.example.orderservice.application.service.OrderService;
import org.example.orderservice.infrastructure.inbox.ProcessedEventService;
import org.example.orderservice.infrastructure.logging.TraceContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationWorkflowListener {

    private final OrderService orderService;
    private final ProcessedEventService processedEventService;

    @KafkaListener(topics = KafkaTopics.RESERVATION_EXPIRED_V1)
    public void handleReservationExpired(ReservationExpiredEvent event) {
        process(event.eventId(), KafkaTopics.RESERVATION_EXPIRED_V1, event.orderId().toString(), event.traceId(),
                () -> {
                    if (event.orderType() == OrderType.COMMON) {
                        orderService.handleCommonReservationExpired(event.orderId());
                    } else {
                        orderService.handleCustomReservationExpired(event.orderId());
                    }
                });
    }

    @KafkaListener(topics = KafkaTopics.ASSEMBLY_COMPLETED_V1)
    public void handleAssemblyCompleted(AssemblyCompletedEvent event) {
        process(event.eventId(), KafkaTopics.ASSEMBLY_COMPLETED_V1, event.orderId().toString(), event.traceId(),
                () -> orderService.handleAssemblyCompleted(event.orderId()));
    }

    @KafkaListener(topics = KafkaTopics.ASSEMBLY_FAILED_V1)
    public void handleAssemblyFailed(AssemblyFailedEvent event) {
        process(event.eventId(), KafkaTopics.ASSEMBLY_FAILED_V1, event.orderId().toString(), event.traceId(),
                () -> orderService.handleAssemblyFailed(event.orderId()));
    }

    private void process(java.util.UUID eventId, String topic, String messageKey, String traceId, Runnable action) {
        TraceContext.runWithTraceId(traceId, () -> {
            boolean processed = processedEventService.processOnce(eventId, topic, messageKey, traceId, action);
            if (!processed) {
                log.info("Skipped duplicate workflow event, eventId={}, topic={}", eventId, topic);
            }
        });
    }
}
