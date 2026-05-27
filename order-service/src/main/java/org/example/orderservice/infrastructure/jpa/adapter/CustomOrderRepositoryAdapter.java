package org.example.orderservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.repository.CustomOrderRepository;
import org.example.orderservice.domain.order.model.CustomCarOrder;
import org.example.orderservice.infrastructure.jpa.repository.JpaCustomOrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CustomOrderRepositoryAdapter implements CustomOrderRepository {

    private final JpaCustomOrderRepository delegate;

    @Override
    public CustomCarOrder save(CustomCarOrder entity) {
        return delegate.save(entity);
    }

    @Override
    public Optional<CustomCarOrder> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public List<CustomCarOrder> findAll() {
        return delegate.findAllByRemovedFalse();
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<CustomCarOrder> existing = delegate.findByIdAndRemovedFalse(id);
        if (existing.isEmpty()) {
            return false;
        }
        CustomCarOrder entity = existing.get();
        entity.setRemoved(true);
        delegate.save(entity);
        return true;
    }

    @Override
    public List<CustomCarOrder> findAllByCustomerId(UUID customerId) {
        return delegate.findAllByCustomerIdAndRemovedFalse(customerId);
    }

    @Override
    public boolean existsByIdAndCustomerId(UUID orderId, UUID customerId) {
        return delegate.existsByIdAndCustomerIdAndRemovedFalse(orderId, customerId);
    }
}