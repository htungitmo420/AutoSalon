package org.example.paymentservice.presentation;

import lombok.RequiredArgsConstructor;
import org.example.paymentservice.application.dto.PaymentIntentResponse;
import org.example.paymentservice.application.service.PaymentService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "payment.provider", havingValue = "mock", matchIfMissing = true)
@RequestMapping("/api/v1/payments/mock")
public class MockPaymentController {
    private final PaymentService paymentService;

    @PostMapping("/{paymentId}/succeed")
    public PaymentIntentResponse succeed(@PathVariable UUID paymentId) {
        return paymentService.succeedMock(paymentId);
    }
}
