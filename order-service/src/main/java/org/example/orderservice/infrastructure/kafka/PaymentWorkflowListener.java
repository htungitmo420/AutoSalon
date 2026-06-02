package org.example.orderservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncontracts.event.PaymentSucceededEvent;
import org.example.commoncontracts.event.PaymentRefundedEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.orderservice.application.service.OrderService;
import org.example.orderservice.application.service.TestDriveService;
import org.example.orderservice.infrastructure.inbox.ProcessedEventService;
import org.example.orderservice.infrastructure.logging.TraceContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentWorkflowListener {

    private final OrderService orderService;
    private final TestDriveService testDriveService;
    private final ProcessedEventService processedEventService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_SUCCEEDED_V1)
    public void handleSucceeded(PaymentSucceededEvent event) {
        TraceContext.runWithTraceId(event.traceId(), () -> {
            boolean processed = processedEventService.processOnce(
                    event.eventId(), KafkaTopics.PAYMENT_SUCCEEDED_V1, event.paymentId().toString(), event.traceId(),
                    () -> apply(event));
            if (!processed) {
                log.info("Skipped duplicate payment event, eventId={}", event.eventId());
            }
        });
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_REFUNDED_V1)
    public void handleRefunded(PaymentRefundedEvent event) {
        TraceContext.runWithTraceId(event.traceId(), () -> {
            boolean processed = processedEventService.processOnce(
                    event.eventId(), KafkaTopics.PAYMENT_REFUNDED_V1, event.paymentId().toString(), event.traceId(),
                    () -> applyRefund(event));
            if (!processed) {
                log.info("Skipped duplicate refund event, eventId={}", event.eventId());
            }
        });
    }

    private void apply(PaymentSucceededEvent event) {
        BigDecimal amount = new BigDecimal(event.amount());
        if (event.targetType() == PaymentTargetType.COMMON_ORDER) {
            orderService.handleCommonPaymentSucceeded(event.referenceId(), event.purpose(), amount);
        } else if (event.targetType() == PaymentTargetType.CUSTOM_ORDER) {
            orderService.handleCustomPaymentSucceeded(event.referenceId(), event.purpose(), amount);
        } else {
            testDriveService.handlePaymentSucceeded(event.referenceId());
        }
    }

    private void applyRefund(PaymentRefundedEvent event) {
        BigDecimal amount = new BigDecimal(event.amount());
        if (event.targetType() == PaymentTargetType.COMMON_ORDER) {
            orderService.handleCommonPaymentRefunded(event.referenceId(), amount);
        } else if (event.targetType() == PaymentTargetType.CUSTOM_ORDER) {
            orderService.handleCustomPaymentRefunded(event.referenceId(), amount);
        } else {
            testDriveService.handlePaymentRefunded(event.referenceId());
        }
    }
}
