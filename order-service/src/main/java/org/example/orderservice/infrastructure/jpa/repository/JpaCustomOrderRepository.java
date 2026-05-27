package org.example.orderservice.infrastructure.jpa.repository;

import org.example.orderservice.domain.order.model.CustomCarOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCustomOrderRepository extends JpaRepository<CustomCarOrder, UUID> {

	Optional<CustomCarOrder> findByIdAndRemovedFalse(UUID id);

	List<CustomCarOrder> findAllByRemovedFalse();

	List<CustomCarOrder> findAllByCustomerIdAndRemovedFalse(UUID customerId);

	boolean existsByIdAndCustomerIdAndRemovedFalse(UUID id, UUID customerId);
}