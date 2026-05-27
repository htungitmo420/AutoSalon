package org.example.orderservice.domain.order.enums;

public enum CustomOrderStatus {
    PENDING_RESERVATION,
    REJECTED,
    CREATED,
    APPROVED_BY_WAREHOUSE,
    WAITING_FOR_PAYMENT,
    PAID,
    ASSEMBLING,
    WAITING_FOR_DELIVERY,
    READY_FOR_PICKUP,
    COMPLETED,
    CANCELLED,
    REFUND_REQUIRED
}
