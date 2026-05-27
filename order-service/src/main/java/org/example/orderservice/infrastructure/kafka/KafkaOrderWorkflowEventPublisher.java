package org.example.orderservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncontracts.event.OrderAwaitingPaymentEvent;
import org.example.commoncontracts.event.OrderCancelledEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.commoncontracts.order.OrderType;
import org.example.orderservice.application.port.out.OrderWorkflowEventPublisher;
import org.example.orderservice.infrastructure.logging.TraceContext;
import org.example.orderservice.infrastructure.outbox.OutboxService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderWorkflowEventPublisher implements OrderWorkflowEventPublisher {

    private static final int EVENT_VERSION = 1;

    private final OutboxService outboxService;

    @Override
    public void publishCommonOrderAwaitingPayment(UUID orderId, UUID reservationId, BigDecimal totalPrice) {
        publishAwaitingPayment(orderId, OrderType.COMMON, reservationId, totalPrice);
    }

    @Override
    public void publishCustomOrderAwaitingPayment(UUID orderId, UUID reservationId, BigDecimal totalPrice) {
        publishAwaitingPayment(orderId, OrderType.CUSTOM, reservationId, totalPrice);
    }

    @Override
    public void publishCommonOrderCancelled(UUID orderId, UUID reservationId) {
        publishCancelled(orderId, OrderType.COMMON, reservationId);
    }

    @Override
    public void publishCustomOrderCancelled(UUID orderId, UUID reservationId) {
        publishCancelled(orderId, OrderType.CUSTOM, reservationId);
    }

    private void publishAwaitingPayment(UUID orderId, OrderType orderType, UUID reservationId, BigDecimal totalPrice) {
        OrderAwaitingPaymentEvent event = new OrderAwaitingPaymentEvent(
                UUID.randomUUID(), EVENT_VERSION, orderId, orderType, reservationId,
                totalPrice == null ? null : totalPrice.toPlainString(), TraceContext.currentTraceId(), Instant.now());
        outboxService.save(KafkaTopics.ORDER_AWAITING_PAYMENT_V1, orderId.toString(), event);
        log.info("Saved order awaiting payment event to outbox, orderId={}", orderId);
    }

    private void publishCancelled(UUID orderId, OrderType orderType, UUID reservationId) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                UUID.randomUUID(), EVENT_VERSION, orderId, orderType, reservationId,
                "Order cancelled", TraceContext.currentTraceId(), Instant.now());
        outboxService.save(KafkaTopics.ORDER_CANCELLED_V1, orderId.toString(), event);
        log.info("Saved order cancelled event to outbox, orderId={}", orderId);
    }
}
