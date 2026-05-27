package org.example.orderservice.application.repository;

import org.example.orderservice.domain.order.model.CommonCarOrder;

import java.util.List;
import java.util.UUID;

public interface CommonOrderRepository extends Repository<CommonCarOrder> {

    List<CommonCarOrder> findAllByCustomerId(UUID customerId);

    boolean existsByIdAndCustomerId(UUID orderId, UUID customerId);
}