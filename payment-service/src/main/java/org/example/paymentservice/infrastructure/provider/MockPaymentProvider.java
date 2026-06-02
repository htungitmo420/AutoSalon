package org.example.paymentservice.infrastructure.provider;

import org.example.paymentservice.application.port.PaymentProvider;
import org.example.paymentservice.domain.payment.PaymentIntent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentProvider implements PaymentProvider {

    public ProviderIntent createIntent(PaymentIntent paymentIntent) {
        String id = "mock_pi_" + paymentIntent.getId();
        return new ProviderIntent(id, "mock_secret_" + paymentIntent.getId(), "requires_payment_method");
    }

    public RefundResult refund(PaymentIntent paymentIntent) {
        // Mock refunds are completed synchronously.
        return new RefundResult(true);
    }

    public CancelResult cancel(PaymentIntent paymentIntent) {
        return new CancelResult(true, false);
    }

    public ProviderWebhook parseWebhook(String payload, String signature) {
        throw new IllegalStateException("Stripe webhook is unavailable when payment.provider=mock");
    }
}
