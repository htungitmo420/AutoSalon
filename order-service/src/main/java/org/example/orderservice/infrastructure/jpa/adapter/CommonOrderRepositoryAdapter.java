package org.example.orderservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.repository.CommonOrderRepository;
import org.example.orderservice.domain.order.model.CommonCarOrder;
import org.example.orderservice.infrastructure.jpa.repository.JpaCommonOrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommonOrderRepositoryAdapter implements CommonOrderRepository {

    private final JpaCommonOrderRepository delegate;

    @Override
    public CommonCarOrder save(CommonCarOrder entity) {
        return delegate.save(entity);
    }

    @Override
    public Optional<CommonCarOrder> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public List<CommonCarOrder> findAll() {
        return delegate.findAllByRemovedFalse();
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<CommonCarOrder> existing = delegate.findByIdAndRemovedFalse(id);
        if (existing.isEmpty()) {
            return false;
        }
        CommonCarOrder entity = existing.get();
        entity.setRemoved(true);
        delegate.save(entity);
        return true;
    }

    @Override
    public List<CommonCarOrder> findAllByCustomerId(UUID customerId) {
        return delegate.findAllByCustomerIdAndRemovedFalse(customerId);
    }

    @Override
    public boolean existsByIdAndCustomerId(UUID orderId, UUID customerId) {
        return delegate.existsByIdAndCustomerIdAndRemovedFalse(orderId, customerId);
    }
}