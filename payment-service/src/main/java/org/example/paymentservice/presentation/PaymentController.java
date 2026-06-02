package org.example.paymentservice.presentation;

import lombok.RequiredArgsConstructor;
import org.example.paymentservice.application.dto.CreatePaymentIntentRequest;
import org.example.paymentservice.application.dto.PaymentIntentResponse;
import org.example.paymentservice.application.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/intents")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentIntentResponse createIntent(
            @RequestBody CreatePaymentIntentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return paymentService.createIntent(request, idempotencyKey);
    }

    @GetMapping("/intents/{paymentId}")
    public PaymentIntentResponse getIntent(@PathVariable UUID paymentId) {
        return paymentService.getIntent(paymentId);
    }

    @PostMapping("/intents/{paymentId}/refund")
    public PaymentIntentResponse refund(@PathVariable UUID paymentId) {
        return paymentService.refund(paymentId);
    }
}
