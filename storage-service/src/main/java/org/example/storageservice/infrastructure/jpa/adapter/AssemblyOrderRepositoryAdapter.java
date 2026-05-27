package org.example.storageservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.repository.AssemblyOrderRepository;
import org.example.storageservice.domain.assembly.model.AssemblyOrder;
import org.example.storageservice.infrastructure.jpa.repository.JpaAssemblyOrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AssemblyOrderRepositoryAdapter implements AssemblyOrderRepository {

    private final JpaAssemblyOrderRepository delegate;

    @Override
    public AssemblyOrder save(AssemblyOrder entity) {
        return delegate.save(entity);
    }

    @Override
    public Optional<AssemblyOrder> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public List<AssemblyOrder> findAll() {
        return delegate.findAllByRemovedFalse();
    }

    @Override
    public List<AssemblyOrder> findAllBySourceOrderId(UUID sourceOrderId) {
        return delegate.findAllBySourceOrderIdAndRemovedFalse(sourceOrderId);
    }

    @Override
    public List<AssemblyOrder> findAllByCarId(UUID carId) {
        return delegate.findAllByCarIdAndRemovedFalse(carId);
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<AssemblyOrder> existing = delegate.findByIdAndRemovedFalse(id);
        if (existing.isEmpty()) {
            return false;
        }
        AssemblyOrder entity = existing.get();
        entity.setRemoved(true);
        delegate.save(entity);
        return true;
    }
}
