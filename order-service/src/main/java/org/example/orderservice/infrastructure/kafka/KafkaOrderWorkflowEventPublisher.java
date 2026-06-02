package org.example.orderservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncontracts.event.OrderAwaitingPaymentEvent;
import org.example.commoncontracts.event.OrderCancelledEvent;
import org.example.commoncontracts.event.TestDriveAwaitingPaymentEvent;
import org.example.commoncontracts.event.TestDriveCancelledEvent;
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
    public void publishCommonOrderAwaitingPayment(UUID orderId, UUID customerId, UUID reservationId,
                                                  BigDecimal totalPrice) {
        publishAwaitingPayment(orderId, customerId, OrderType.COMMON, reservationId, totalPrice);
    }

    @Override
    public void publishCustomOrderAwaitingPayment(UUID orderId, UUID customerId, UUID reservationId,
                                                  BigDecimal totalPrice) {
        publishAwaitingPayment(orderId, customerId, OrderType.CUSTOM, reservationId, totalPrice);
    }

    @Override
    public void publishTestDriveAwaitingPayment(UUID testDriveId, UUID customerId, BigDecimal amount) {
        TestDriveAwaitingPaymentEvent event = new TestDriveAwaitingPaymentEvent(
                UUID.randomUUID(), EVENT_VERSION, testDriveId, customerId, amount.toPlainString(),
                TraceContext.currentTraceId(), Instant.now());
        outboxService.save(KafkaTopics.TEST_DRIVE_AWAITING_PAYMENT_V1, testDriveId.toString(), event);
        log.info("Saved test-drive awaiting payment event to outbox, testDriveId={}", testDriveId);
    }

    @Override
    public void publishTestDriveCancelled(UUID testDriveId, UUID customerId) {
        TestDriveCancelledEvent event = new TestDriveCancelledEvent(
                UUID.randomUUID(), EVENT_VERSION, testDriveId, customerId,
                TraceContext.currentTraceId(), Instant.now());
        outboxService.save(KafkaTopics.TEST_DRIVE_CANCELLED_V1, testDriveId.toString(), event);
        log.info("Saved test-drive cancelled event to outbox, testDriveId={}", testDriveId);
    }

    @Override
    public void publishCommonOrderCancelled(UUID orderId, UUID reservationId) {
        publishCancelled(orderId, OrderType.COMMON, reservationId);
    }

    @Override
    public void publishCustomOrderCancelled(UUID orderId, UUID reservationId) {
        publishCancelled(orderId, OrderType.CUSTOM, reservationId);
    }

    private void publishAwaitingPayment(UUID orderId, UUID customerId, OrderType orderType, UUID reservationId,
                                        BigDecimal totalPrice) {
        OrderAwaitingPaymentEvent event = new OrderAwaitingPaymentEvent(
                UUID.randomUUID(), EVENT_VERSION, orderId, customerId, orderType, reservationId,
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
