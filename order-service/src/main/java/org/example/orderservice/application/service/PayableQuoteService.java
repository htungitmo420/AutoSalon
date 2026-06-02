package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.orderservice.application.repository.CommonOrderRepository;
import org.example.orderservice.application.repository.CustomOrderRepository;
import org.example.orderservice.application.repository.TestDriveRepository;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayableQuoteService {
    private final CommonOrderRepository commonOrderRepository;
    private final CustomOrderRepository customOrderRepository;
    private final TestDriveRepository testDriveRepository;

    @Transactional(readOnly = true)
    public PayableQuote getQuote(PaymentTargetType targetType, UUID referenceId, UUID customerId) {
        return switch (targetType) {
            case COMMON_ORDER -> commonOrderRepository.findById(referenceId)
                    .filter(order -> order.getCustomerId().equals(customerId))
                    .map(order -> new PayableQuote(targetType, referenceId, customerId, order.getTotalPrice(),
                            Set.of(CommonOrderStatus.WAITING_FOR_PAYMENT, CommonOrderStatus.DEPOSIT_PAID)
                                    .contains(order.getStatus())))
                    .orElseThrow(() -> notFound(referenceId));
            case CUSTOM_ORDER -> customOrderRepository.findById(referenceId)
                    .filter(order -> order.getCustomerId().equals(customerId))
                    .map(order -> new PayableQuote(targetType, referenceId, customerId, order.getTotalPrice(),
                            Set.of(CustomOrderStatus.WAITING_FOR_PAYMENT, CustomOrderStatus.ASSEMBLING,
                                    CustomOrderStatus.WAITING_FOR_BALANCE).contains(order.getStatus())))
                    .orElseThrow(() -> notFound(referenceId));
            case TEST_DRIVE -> testDriveRepository.findById(referenceId)
                    .filter(testDrive -> testDrive.getCustomerId().equals(customerId))
                    .map(testDrive -> new PayableQuote(targetType, referenceId, customerId,
                            testDrive.getFee() == null ? BigDecimal.ZERO : testDrive.getFee(),
                            testDrive.getStatus() == TestDriveStatus.WAITING_FOR_PAYMENT
                                    && testDrive.getFee() != null
                                    && testDrive.getFee().signum() > 0))
                    .orElseThrow(() -> notFound(referenceId));
        };
    }

    private EntityNotFoundException notFound(UUID referenceId) {
        return new EntityNotFoundException("Payable reference not found: " + referenceId);
    }

    public record PayableQuote(PaymentTargetType targetType, UUID referenceId, UUID customerId,
                               BigDecimal totalAmount, boolean active) {
    }
}
