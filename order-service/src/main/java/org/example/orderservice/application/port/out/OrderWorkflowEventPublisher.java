package org.example.orderservice.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface OrderWorkflowEventPublisher {

    void publishCommonOrderAwaitingPayment(UUID orderId, UUID reservationId, BigDecimal totalPrice);

    void publishCustomOrderAwaitingPayment(UUID orderId, UUID reservationId, BigDecimal totalPrice);

    void publishCommonOrderCancelled(UUID orderId, UUID reservationId);

    void publishCustomOrderCancelled(UUID orderId, UUID reservationId);
}
