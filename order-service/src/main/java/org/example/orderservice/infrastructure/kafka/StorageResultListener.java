package org.example.orderservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncontracts.event.OrderApprovedEvent;
import org.example.commoncontracts.event.OrderRejectedEvent;
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
public class StorageResultListener {

    private final OrderService orderService;
    private final ProcessedEventService processedEventService;

    @KafkaListener(topics = KafkaTopics.ORDER_APPROVED)
    public void handleApproved(OrderApprovedEvent event) {
        TraceContext.runWithTraceId(event.traceId(), () -> {
            log.info("Received order approved event, eventId={}, orderId={}", event.eventId(), event.orderId());

            boolean processed = processedEventService.processOnce(
                    event.eventId(),
                    KafkaTopics.ORDER_APPROVED,
                    event.orderId().toString(),
                    event.traceId(),
                    () -> {
                        if (event.orderType() == OrderType.COMMON) {
                            orderService.handleCommonStorageApproved(event.orderId());
                        } else {
                            orderService.handleCustomStorageApproved(event.orderId());
                        }
                    });

            if (!processed) {
                log.info("Skipped duplicate order approved event, eventId={}", event.eventId());
            }
        });
    }

    @KafkaListener(topics = KafkaTopics.ORDER_REJECTED)
    public void handleRejected(OrderRejectedEvent event) {
        TraceContext.runWithTraceId(event.traceId(), () -> {
            log.info("Received order rejected event, eventId={}, orderId={}", event.eventId(), event.orderId());

            boolean processed = processedEventService.processOnce(
                    event.eventId(),
                    KafkaTopics.ORDER_REJECTED,
                    event.orderId().toString(),
                    event.traceId(),
                    () -> {
                        if (event.orderType() == OrderType.COMMON) {
                            orderService.handleCommonStorageRejected(event.orderId());
                        } else {
                            orderService.handleCustomStorageRejected(event.orderId());
                        }
                    });

            if (!processed) {
                log.info("Skipped duplicate order rejected event, eventId={}", event.eventId());
            }
        });
    }
}
