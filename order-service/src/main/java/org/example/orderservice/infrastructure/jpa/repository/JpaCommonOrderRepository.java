package org.example.orderservice.infrastructure.jpa.repository;

import org.example.orderservice.domain.order.model.CommonCarOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCommonOrderRepository extends JpaRepository<CommonCarOrder, UUID> {

	Optional<CommonCarOrder> findByIdAndRemovedFalse(UUID id);

	List<CommonCarOrder> findAllByRemovedFalse();

	List<CommonCarOrder> findAllByCustomerIdAndRemovedFalse(UUID customerId);

	boolean existsByIdAndCustomerIdAndRemovedFalse(UUID id, UUID customerId);
}