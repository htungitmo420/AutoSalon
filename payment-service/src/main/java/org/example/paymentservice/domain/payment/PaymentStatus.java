package org.example.paymentservice.domain.payment;

public enum PaymentStatus {
    CREATING,
    REQUIRES_PAYMENT_METHOD,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    REFUND_PENDING,
    REFUNDED
}
