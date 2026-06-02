package org.example.paymentservice.application.port;

import org.example.paymentservice.domain.payment.PaymentIntent;

public interface PaymentProvider {

    ProviderIntent createIntent(PaymentIntent paymentIntent);

    RefundResult refund(PaymentIntent paymentIntent);

    CancelResult cancel(PaymentIntent paymentIntent);

    ProviderWebhook parseWebhook(String payload, String signature);

    record ProviderIntent(String id, String clientSecret, String status) {
    }

    record RefundResult(boolean completed) {
    }

    record CancelResult(boolean cancelled, boolean paymentSucceeded) {
    }

    record ProviderWebhook(String providerEventId, String providerPaymentIntentId, Type type, String failureMessage) {
        public enum Type {
            SUCCEEDED,
            FAILED,
            REFUNDED,
            IGNORED
        }
    }
}
