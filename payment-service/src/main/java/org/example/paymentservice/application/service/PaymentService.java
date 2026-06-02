package org.example.paymentservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.commoncontracts.event.PaymentFailedEvent;
import org.example.commoncontracts.event.PaymentRefundedEvent;
import org.example.commoncontracts.event.PaymentSucceededEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.commoncontracts.payment.PaymentPurpose;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.application.dto.CreatePaymentIntentRequest;
import org.example.paymentservice.application.dto.PaymentIntentResponse;
import org.example.paymentservice.application.mapper.PaymentMapper;
import org.example.paymentservice.application.port.CurrentUserProvider;
import org.example.paymentservice.application.port.PaymentProvider;
import org.example.paymentservice.application.port.PayableQuoteClient;
import org.example.paymentservice.application.repository.PayableReferenceRepository;
import org.example.paymentservice.application.repository.PaymentIntentRepository;
import org.example.paymentservice.domain.exceptions.DomainValidationException;
import org.example.paymentservice.domain.exceptions.EntityNotFoundException;
import org.example.paymentservice.domain.payment.PayableReference;
import org.example.paymentservice.domain.payment.PaymentIntent;
import org.example.paymentservice.domain.payment.PaymentStatus;
import org.example.paymentservice.infrastructure.logging.TraceContext;
import org.example.paymentservice.infrastructure.outbox.OutboxService;
import org.example.paymentservice.infrastructure.webhook.ProcessedWebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final int EVENT_VERSION = 1;

    private final PaymentIntentRepository paymentIntentRepository;
    private final PayableReferenceRepository payableReferenceRepository;
    private final PaymentProvider paymentProvider;
    private final PayableQuoteClient payableQuoteClient;
    private final CurrentUserProvider currentUserProvider;
    private final ProcessedWebhookService processedWebhookService;
    private final OutboxService outboxService;

    @Value("${payment.currency:usd}")
    private String currency;

    @Value("${payment.deposit-percent:10}")
    private BigDecimal depositPercent;

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public PaymentIntentResponse createIntent(CreatePaymentIntentRequest request, String idempotencyKey) {
        validateRequest(request, idempotencyKey);
        UUID customerId = currentUserProvider.getCurrentUserId();
        PaymentIntent existing = paymentIntentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            validateIdempotentRetry(existing, request, customerId);
            return PaymentMapper.INSTANCE.toResponse(existing);
        }

        PayableQuoteClient.PayableQuote quote =
                payableQuoteClient.getQuote(request.targetType(), request.referenceId(), customerId);
        if (!quote.active()) {
            throw new DomainValidationException("Payable reference is no longer active");
        }
        PayableReference payable = PayableReference.builder()
                .targetType(quote.targetType())
                .referenceId(quote.referenceId())
                .customerId(quote.customerId())
                .totalAmount(quote.totalAmount())
                .active(true)
                .build();

        BigDecimal amount = calculateAmount(payable, request.purpose());
        PaymentIntent paymentIntent = PaymentIntent.builder()
                .referenceId(request.referenceId())
                .customerId(customerId)
                .targetType(request.targetType())
                .purpose(request.purpose())
                .amount(amount)
                .currency(currency.toLowerCase())
                .status(PaymentStatus.CREATING)
                .idempotencyKey(idempotencyKey)
                .build();
        paymentIntent = paymentIntentRepository.save(paymentIntent);

        PaymentProvider.ProviderIntent providerIntent = paymentProvider.createIntent(paymentIntent);
        paymentIntent.setProviderPaymentIntentId(providerIntent.id());
        paymentIntent.setClientSecret(providerIntent.clientSecret());
        paymentIntent.setStatus(mapProviderStatus(providerIntent.status()));
        return PaymentMapper.INSTANCE.toResponse(paymentIntentRepository.save(paymentIntent));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public PaymentIntentResponse getIntent(UUID paymentId) {
        PaymentIntent paymentIntent = findPayment(paymentId);
        if (!paymentIntent.getCustomerId().equals(currentUserProvider.getCurrentUserId())) {
            throw new EntityNotFoundException("Payment not found");
        }
        return PaymentMapper.INSTANCE.toResponse(paymentIntent);
    }

    @Transactional
    public void handleStripeWebhook(String payload, String signature) {
        PaymentProvider.ProviderWebhook webhook = paymentProvider.parseWebhook(payload, signature);
        if (webhook.type() == PaymentProvider.ProviderWebhook.Type.IGNORED) {
            return;
        }
        processedWebhookService.processOnce(webhook.providerEventId(), () -> applyWebhook(webhook));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentIntentResponse refund(UUID paymentId) {
        PaymentIntent paymentIntent = findPayment(paymentId);
        if (paymentIntent.getStatus() == PaymentStatus.REFUNDED) {
            return PaymentMapper.INSTANCE.toResponse(paymentIntent);
        }
        if (paymentIntent.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new DomainValidationException("Only a succeeded payment can be refunded");
        }
        PaymentProvider.RefundResult refund = paymentProvider.refund(paymentIntent);
        deactivatePayableReference(paymentIntent);
        if (refund.completed()) {
            completeRefund(paymentIntent);
        } else {
            paymentIntent.setStatus(PaymentStatus.REFUND_PENDING);
        }
        return PaymentMapper.INSTANCE.toResponse(paymentIntentRepository.save(paymentIntent));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentIntentResponse succeedMock(UUID paymentId) {
        PaymentIntent paymentIntent = findPayment(paymentId);
        if (paymentIntent.getStatus() != PaymentStatus.SUCCEEDED) {
            paymentIntent.setStatus(PaymentStatus.SUCCEEDED);
            publishSucceeded(paymentIntent);
            paymentIntentRepository.save(paymentIntent);
        }
        return PaymentMapper.INSTANCE.toResponse(paymentIntent);
    }

    @Transactional
    public void cancelPendingForReference(PaymentTargetType targetType, UUID referenceId) {
        Set<PaymentStatus> pendingStatuses = Set.of(
                PaymentStatus.CREATING,
                PaymentStatus.REQUIRES_PAYMENT_METHOD,
                PaymentStatus.PROCESSING);
        paymentIntentRepository.findByReferenceAndStatuses(referenceId, targetType, pendingStatuses)
                .forEach(this::cancelPending);
    }

    private void applyWebhook(PaymentProvider.ProviderWebhook webhook) {
        PaymentIntent paymentIntent = paymentIntentRepository.findByProviderPaymentIntentId(webhook.providerPaymentIntentId())
                .orElseThrow(() -> new EntityNotFoundException("Payment provider intent not found"));
        if (webhook.type() == PaymentProvider.ProviderWebhook.Type.SUCCEEDED
                && paymentIntent.getStatus() != PaymentStatus.SUCCEEDED) {
            paymentIntent.setStatus(PaymentStatus.SUCCEEDED);
            publishSucceeded(paymentIntent);
        } else if (webhook.type() == PaymentProvider.ProviderWebhook.Type.FAILED) {
            paymentIntent.setStatus(PaymentStatus.FAILED);
            paymentIntent.setFailureMessage(webhook.failureMessage());
            publishFailed(paymentIntent);
        } else if (webhook.type() == PaymentProvider.ProviderWebhook.Type.REFUNDED
                && paymentIntent.getStatus() != PaymentStatus.REFUNDED) {
            deactivatePayableReference(paymentIntent);
            completeRefund(paymentIntent);
        }
        paymentIntentRepository.save(paymentIntent);
    }

    private void deactivatePayableReference(PaymentIntent paymentIntent) {
        payableReferenceRepository.find(paymentIntent.getTargetType(), paymentIntent.getReferenceId())
                .ifPresent(payable -> {
                    payable.setActive(false);
                    payableReferenceRepository.save(payable);
                });
    }

    private void completeRefund(PaymentIntent paymentIntent) {
        paymentIntent.setStatus(PaymentStatus.REFUNDED);
        publishRefunded(paymentIntent);
    }

    private void cancelPending(PaymentIntent paymentIntent) {
        if (paymentIntent.getProviderPaymentIntentId() == null) {
            paymentIntent.setStatus(PaymentStatus.CANCELLED);
            paymentIntentRepository.save(paymentIntent);
            return;
        }
        PaymentProvider.CancelResult result = paymentProvider.cancel(paymentIntent);
        if (result.cancelled()) {
            paymentIntent.setStatus(PaymentStatus.CANCELLED);
        } else if (result.paymentSucceeded()) {
            paymentIntent.setStatus(PaymentStatus.SUCCEEDED);
            PaymentProvider.RefundResult refund = paymentProvider.refund(paymentIntent);
            if (refund.completed()) {
                completeRefund(paymentIntent);
            } else {
                paymentIntent.setStatus(PaymentStatus.REFUND_PENDING);
            }
        }
        paymentIntentRepository.save(paymentIntent);
    }

    private BigDecimal calculateAmount(PayableReference payable, PaymentPurpose purpose) {
        if (payable.getTargetType() == PaymentTargetType.TEST_DRIVE) {
            if (purpose != PaymentPurpose.TEST_DRIVE_FEE) {
                throw new DomainValidationException("Test drive only accepts TEST_DRIVE_FEE");
            }
            return payable.getTotalAmount();
        }
        if (purpose == PaymentPurpose.TEST_DRIVE_FEE) {
            throw new DomainValidationException("Car order does not accept TEST_DRIVE_FEE");
        }

        BigDecimal paid = paymentIntentRepository.sumAmountByReferenceAndStatus(
                payable.getReferenceId(), payable.getTargetType(), PaymentStatus.SUCCEEDED);
        BigDecimal remaining = payable.getTotalAmount().subtract(paid);
        if (remaining.signum() <= 0) {
            throw new DomainValidationException("Reference is already fully paid");
        }
        if (purpose == PaymentPurpose.CAR_FULL_PAYMENT) {
            return remaining;
        }
        if (paymentIntentRepository.existsSucceededDeposit(payable.getReferenceId(), payable.getTargetType())) {
            throw new DomainValidationException("Deposit was already paid");
        }
        return payable.getTotalAmount()
                .multiply(depositPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                .min(remaining);
    }

    private void publishSucceeded(PaymentIntent paymentIntent) {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                UUID.randomUUID(), EVENT_VERSION, paymentIntent.getId(), paymentIntent.getReferenceId(),
                paymentIntent.getCustomerId(), paymentIntent.getTargetType(), paymentIntent.getPurpose(),
                paymentIntent.getAmount().toPlainString(), paymentIntent.getCurrency(),
                TraceContext.currentTraceId(), Instant.now());
        outboxService.save(KafkaTopics.PAYMENT_SUCCEEDED_V1, paymentIntent.getReferenceId().toString(), event);
    }

    private void publishFailed(PaymentIntent paymentIntent) {
        PaymentFailedEvent event = new PaymentFailedEvent(
                UUID.randomUUID(), EVENT_VERSION, paymentIntent.getId(), paymentIntent.getReferenceId(),
                paymentIntent.getCustomerId(), paymentIntent.getTargetType(), paymentIntent.getPurpose(),
                paymentIntent.getFailureMessage(), TraceContext.currentTraceId(), Instant.now());
        outboxService.save(KafkaTopics.PAYMENT_FAILED_V1, paymentIntent.getReferenceId().toString(), event);
    }

    private void publishRefunded(PaymentIntent paymentIntent) {
        PaymentRefundedEvent event = new PaymentRefundedEvent(
                UUID.randomUUID(), EVENT_VERSION, paymentIntent.getId(), paymentIntent.getReferenceId(),
                paymentIntent.getCustomerId(), paymentIntent.getTargetType(), paymentIntent.getPurpose(),
                paymentIntent.getAmount().toPlainString(), paymentIntent.getCurrency(),
                TraceContext.currentTraceId(), Instant.now());
        outboxService.save(KafkaTopics.PAYMENT_REFUNDED_V1, paymentIntent.getReferenceId().toString(), event);
    }

    private void validateRequest(CreatePaymentIntentRequest request, String idempotencyKey) {
        if (request == null || request.targetType() == null || request.referenceId() == null || request.purpose() == null) {
            throw new DomainValidationException("targetType, referenceId and purpose are required");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new DomainValidationException("Idempotency-Key header is required and must not exceed 255 characters");
        }
    }

    private void validateIdempotentRetry(PaymentIntent existing, CreatePaymentIntentRequest request, UUID customerId) {
        if (!existing.getCustomerId().equals(customerId)
                || !existing.getReferenceId().equals(request.referenceId())
                || existing.getTargetType() != request.targetType()
                || existing.getPurpose() != request.purpose()) {
            throw new DomainValidationException("Idempotency-Key was already used for a different request");
        }
    }

    private PaymentStatus mapProviderStatus(String status) {
        return switch (status) {
            case "processing" -> PaymentStatus.PROCESSING;
            case "succeeded" -> PaymentStatus.SUCCEEDED;
            default -> PaymentStatus.REQUIRES_PAYMENT_METHOD;
        };
    }

    private PaymentIntent findPayment(UUID paymentId) {
        return paymentIntentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));
    }
}
