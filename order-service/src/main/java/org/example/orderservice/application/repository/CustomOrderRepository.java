package org.example.orderservice.application.repository;

import org.example.orderservice.domain.order.model.CustomCarOrder;

import java.util.List;
import java.util.UUID;

public interface CustomOrderRepository extends Repository<CustomCarOrder> {

    List<CustomCarOrder> findAllByCustomerId(UUID customerId);

    boolean existsByIdAndCustomerId(UUID orderId, UUID customerId);
}