package org.example.orderservice.domain.order.enums;

public enum CommonOrderStatus {
    PENDING_RESERVATION,
    REJECTED,
    CREATED,
    APPROVED_BY_MANAGER,
    WAITING_FOR_PAYMENT,
    DEPOSIT_PAID,
    PAID,
    READY_FOR_PICKUP,
    COMPLETED,
    CANCELLED,
    REFUND_REQUIRED
}
