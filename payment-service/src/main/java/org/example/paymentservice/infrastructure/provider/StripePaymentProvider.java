package org.example.paymentservice.infrastructure.provider;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.example.paymentservice.application.port.PaymentProvider;
import org.example.paymentservice.domain.payment.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
public class StripePaymentProvider implements PaymentProvider {

    private final String webhookSecret;
    private final int currencyDecimalPlaces;

    public StripePaymentProvider(
            @Value("${payment.stripe.secret-key}") String secretKey,
            @Value("${payment.stripe.webhook-secret}") String webhookSecret,
            @Value("${payment.currency-decimal-places:2}") int currencyDecimalPlaces) {
        if (secretKey == null || secretKey.isBlank() || webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("Stripe secret key and webhook secret are required");
        }
        Stripe.apiKey = secretKey;
        this.webhookSecret = webhookSecret;
        this.currencyDecimalPlaces = currencyDecimalPlaces;
    }

    @Override
    public ProviderIntent createIntent(PaymentIntent paymentIntent) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(toMinorUnits(paymentIntent))
                    .setCurrency(paymentIntent.getCurrency())
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build())
                    .putMetadata("payment_id", paymentIntent.getId().toString())
                    .putMetadata("reference_id", paymentIntent.getReferenceId().toString())
                    .putMetadata("target_type", paymentIntent.getTargetType().name())
                    .putMetadata("purpose", paymentIntent.getPurpose().name())
                    .build();
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey(paymentIntent.getIdempotencyKey())
                    .build();
            com.stripe.model.PaymentIntent stripeIntent = com.stripe.model.PaymentIntent.create(params, options);
            return new ProviderIntent(stripeIntent.getId(), stripeIntent.getClientSecret(), stripeIntent.getStatus());
        } catch (Exception exception) {
            throw new IllegalStateException("Stripe PaymentIntent creation failed", exception);
        }
    }

    @Override
    public RefundResult refund(PaymentIntent paymentIntent) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntent.getProviderPaymentIntentId())
                    .build();
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("refund-" + paymentIntent.getId())
                    .build();
            Refund refund = Refund.create(params, options);
            return new RefundResult("succeeded".equals(refund.getStatus()));
        } catch (Exception exception) {
            throw new IllegalStateException("Stripe refund failed", exception);
        }
    }

    @Override
    public CancelResult cancel(PaymentIntent paymentIntent) {
        try {
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("cancel-" + paymentIntent.getId())
                    .build();
            com.stripe.model.PaymentIntent.retrieve(paymentIntent.getProviderPaymentIntentId()).cancel(options);
            return new CancelResult(true, false);
        } catch (Exception exception) {
            try {
                com.stripe.model.PaymentIntent providerIntent =
                        com.stripe.model.PaymentIntent.retrieve(paymentIntent.getProviderPaymentIntentId());
                if ("canceled".equals(providerIntent.getStatus())) {
                    return new CancelResult(true, false);
                }
                if ("succeeded".equals(providerIntent.getStatus())) {
                    return new CancelResult(false, true);
                }
            } catch (Exception retrieveException) {
                exception.addSuppressed(retrieveException);
            }
            throw new IllegalStateException("Stripe PaymentIntent cancellation failed", exception);
        }
    }

    @Override
    public ProviderWebhook parseWebhook(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            Object object = event.getDataObjectDeserializer().getObject().orElse(null);
            if ("refund.updated".equals(event.getType())
                    && object instanceof Refund refund
                    && "succeeded".equals(refund.getStatus())) {
                return new ProviderWebhook(event.getId(), refund.getPaymentIntent(),
                        ProviderWebhook.Type.REFUNDED, null);
            }
            if (object instanceof com.stripe.model.PaymentIntent paymentIntent) {
                return switch (event.getType()) {
                    case "payment_intent.succeeded" -> new ProviderWebhook(
                            event.getId(), paymentIntent.getId(), ProviderWebhook.Type.SUCCEEDED, null);
                    case "payment_intent.payment_failed" -> new ProviderWebhook(
                            event.getId(), paymentIntent.getId(), ProviderWebhook.Type.FAILED,
                            paymentIntent.getLastPaymentError() == null
                                    ? "Stripe payment failed"
                                    : paymentIntent.getLastPaymentError().getMessage());
                    default -> new ProviderWebhook(event.getId(), paymentIntent.getId(),
                            ProviderWebhook.Type.IGNORED, null);
                };
            }
            return new ProviderWebhook(event.getId(), "", ProviderWebhook.Type.IGNORED, null);
        } catch (SignatureVerificationException exception) {
            throw new IllegalArgumentException("Invalid Stripe webhook signature", exception);
        }
    }

    private long toMinorUnits(PaymentIntent paymentIntent) {
        return paymentIntent.getAmount()
                .movePointRight(currencyDecimalPlaces)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();
    }
}
