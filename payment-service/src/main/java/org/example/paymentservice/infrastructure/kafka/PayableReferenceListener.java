package org.example.paymentservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncontracts.event.OrderAwaitingPaymentEvent;
import org.example.commoncontracts.event.TestDriveAwaitingPaymentEvent;
import org.example.commoncontracts.event.TestDriveCancelledEvent;
import org.example.commoncontracts.event.OrderCancelledEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.commoncontracts.order.OrderType;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.application.repository.PayableReferenceRepository;
import org.example.paymentservice.application.service.PaymentService;
import org.example.paymentservice.domain.payment.PayableReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayableReferenceListener {

    private final PayableReferenceRepository payableReferenceRepository;
    private final PaymentService paymentService;

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_AWAITING_PAYMENT_V1)
    public void handleOrder(OrderAwaitingPaymentEvent event) {
        if (event.customerId() == null || event.totalPrice() == null) {
            log.warn("Skipping legacy order payment event without customer or price, orderId={}", event.orderId());
            return;
        }
        PaymentTargetType targetType = event.orderType() == OrderType.COMMON
                ? PaymentTargetType.COMMON_ORDER
                : PaymentTargetType.CUSTOM_ORDER;
        upsert(targetType, event.orderId(), event.customerId(), new BigDecimal(event.totalPrice()));
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.TEST_DRIVE_AWAITING_PAYMENT_V1)
    public void handleTestDrive(TestDriveAwaitingPaymentEvent event) {
        upsert(PaymentTargetType.TEST_DRIVE, event.testDriveId(), event.customerId(), new BigDecimal(event.amount()));
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED_V1)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        PaymentTargetType targetType = event.orderType() == OrderType.COMMON
                ? PaymentTargetType.COMMON_ORDER
                : PaymentTargetType.CUSTOM_ORDER;
        deactivate(targetType, event.orderId());
        paymentService.cancelPendingForReference(targetType, event.orderId());
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.TEST_DRIVE_CANCELLED_V1)
    public void handleTestDriveCancelled(TestDriveCancelledEvent event) {
        deactivate(PaymentTargetType.TEST_DRIVE, event.testDriveId());
        paymentService.cancelPendingForReference(PaymentTargetType.TEST_DRIVE, event.testDriveId());
    }

    private void upsert(PaymentTargetType targetType, java.util.UUID referenceId,
                        java.util.UUID customerId, BigDecimal totalAmount) {
        PayableReference payable = payableReferenceRepository.find(targetType, referenceId)
                .orElseGet(() -> PayableReference.builder()
                        .targetType(targetType)
                        .referenceId(referenceId)
                        .build());
        payable.setCustomerId(customerId);
        payable.setTotalAmount(totalAmount);
        payable.setActive(true);
        payableReferenceRepository.save(payable);
    }

    private void deactivate(PaymentTargetType targetType, java.util.UUID referenceId) {
        payableReferenceRepository.find(targetType, referenceId).ifPresent(payable -> {
            payable.setActive(false);
            payableReferenceRepository.save(payable);
        });
    }
}
