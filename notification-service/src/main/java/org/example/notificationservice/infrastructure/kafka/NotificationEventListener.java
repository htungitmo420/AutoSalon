package org.example.notificationservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.example.commoncontracts.event.AssemblyCompletedEvent;
import org.example.commoncontracts.event.AssemblyFailedEvent;
import org.example.commoncontracts.event.OrderAwaitingPaymentEvent;
import org.example.commoncontracts.event.OrderCancelledEvent;
import org.example.commoncontracts.event.PaymentFailedEvent;
import org.example.commoncontracts.event.PaymentRefundedEvent;
import org.example.commoncontracts.event.PaymentSucceededEvent;
import org.example.commoncontracts.event.ReservationExpiredEvent;
import org.example.commoncontracts.event.TestDriveAwaitingPaymentEvent;
import org.example.commoncontracts.event.TestDriveCancelledEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.notificationservice.application.service.NotificationProjectionService;
import org.example.notificationservice.domain.notification.NotificationType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationProjectionService projectionService;

    @KafkaListener(topics = KafkaTopics.ORDER_AWAITING_PAYMENT_V1)
    public void onOrderAwaitingPayment(OrderAwaitingPaymentEvent event) {
        project(event.eventId(), KafkaTopics.ORDER_AWAITING_PAYMENT_V1, event.orderId(), event.customerId(),
                event.orderType().name(), NotificationType.ORDER_AWAITING_PAYMENT,
                "Order awaiting payment", "Your order is ready for payment.", event.traceId(), event.occurredAt(),
                amount(event.totalPrice()));
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED_V1)
    public void onOrderCancelled(OrderCancelledEvent event) {
        project(event.eventId(), KafkaTopics.ORDER_CANCELLED_V1, event.orderId(), null,
                event.orderType().name(), NotificationType.ORDER_CANCELLED,
                "Order cancelled", "Your order has been cancelled.", event.traceId(), event.occurredAt(), null);
    }

    @KafkaListener(topics = KafkaTopics.RESERVATION_EXPIRED_V1)
    public void onReservationExpired(ReservationExpiredEvent event) {
        project(event.eventId(), KafkaTopics.RESERVATION_EXPIRED_V1, event.orderId(), null,
                event.orderType().name(), NotificationType.RESERVATION_EXPIRED,
                "Reservation expired", "Your inventory reservation expired before payment.", event.traceId(),
                event.occurredAt(), null);
    }

    @KafkaListener(topics = KafkaTopics.ASSEMBLY_COMPLETED_V1)
    public void onAssemblyCompleted(AssemblyCompletedEvent event) {
        project(event.eventId(), KafkaTopics.ASSEMBLY_COMPLETED_V1, event.orderId(), null,
                "CUSTOM_ORDER", NotificationType.ASSEMBLY_COMPLETED,
                "Assembly completed", "Your vehicle assembly has completed.", event.traceId(), event.occurredAt(), null);
    }

    @KafkaListener(topics = KafkaTopics.ASSEMBLY_FAILED_V1)
    public void onAssemblyFailed(AssemblyFailedEvent event) {
        project(event.eventId(), KafkaTopics.ASSEMBLY_FAILED_V1, event.orderId(), null,
                "CUSTOM_ORDER", NotificationType.ASSEMBLY_FAILED,
                "Assembly failed", "Your vehicle assembly could not be completed.", event.traceId(), event.occurredAt(), null);
    }

    @KafkaListener(topics = KafkaTopics.TEST_DRIVE_AWAITING_PAYMENT_V1)
    public void onTestDriveAwaitingPayment(TestDriveAwaitingPaymentEvent event) {
        project(event.eventId(), KafkaTopics.TEST_DRIVE_AWAITING_PAYMENT_V1, event.testDriveId(), event.customerId(),
                "TEST_DRIVE", NotificationType.TEST_DRIVE_AWAITING_PAYMENT,
                "Test drive awaiting payment", "Your test drive fee is ready for payment.", event.traceId(),
                event.occurredAt(), amount(event.amount()));
    }

    @KafkaListener(topics = KafkaTopics.TEST_DRIVE_CANCELLED_V1)
    public void onTestDriveCancelled(TestDriveCancelledEvent event) {
        project(event.eventId(), KafkaTopics.TEST_DRIVE_CANCELLED_V1, event.testDriveId(), event.customerId(),
                "TEST_DRIVE", NotificationType.TEST_DRIVE_CANCELLED,
                "Test drive cancelled", "Your test drive booking has been cancelled.", event.traceId(), event.occurredAt(), null);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_SUCCEEDED_V1)
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        project(event.eventId(), KafkaTopics.PAYMENT_SUCCEEDED_V1, event.referenceId(), event.customerId(),
                event.targetType().name(), NotificationType.PAYMENT_SUCCEEDED,
                "Payment succeeded", "Your payment has been confirmed.", event.traceId(), event.occurredAt(),
                amount(event.amount()));
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED_V1)
    public void onPaymentFailed(PaymentFailedEvent event) {
        project(event.eventId(), KafkaTopics.PAYMENT_FAILED_V1, event.referenceId(), event.customerId(),
                event.targetType().name(), NotificationType.PAYMENT_FAILED,
                "Payment failed", "Your payment could not be completed.", event.traceId(), event.occurredAt(), null);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_REFUNDED_V1)
    public void onPaymentRefunded(PaymentRefundedEvent event) {
        project(event.eventId(), KafkaTopics.PAYMENT_REFUNDED_V1, event.referenceId(), event.customerId(),
                event.targetType().name(), NotificationType.PAYMENT_REFUNDED,
                "Payment refunded", "Your refund has been confirmed.", event.traceId(), event.occurredAt(),
                amount(event.amount()));
    }

    private void project(java.util.UUID eventId, String topic, java.util.UUID referenceId, java.util.UUID customerId,
                         String referenceType, NotificationType type, String title, String message, String traceId,
                         java.time.Instant occurredAt, BigDecimal amount) {
        projectionService.project(eventId, topic, referenceId, customerId, referenceType, type, title, message,
                traceId, occurredAt, amount);
    }

    private BigDecimal amount(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
