package org.example.paymentservice.presentation;

import lombok.RequiredArgsConstructor;
import org.example.paymentservice.application.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/webhooks")
public class StripeWebhookController {
    private final PaymentService paymentService;

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripe(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        paymentService.handleStripeWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
