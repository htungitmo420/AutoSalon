package org.example.storageservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.repository.PartStockRepository;
import org.example.storageservice.domain.stock.model.PartStock;
import org.example.storageservice.infrastructure.jpa.repository.JpaPartStockRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PartStockRepositoryAdapter implements PartStockRepository {

    private final JpaPartStockRepository delegate;

    @Override
    public PartStock save(PartStock entity) {
        return delegate.save(entity);
    }

    @Override
    public Optional<PartStock> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public Optional<PartStock> findByPartId(UUID partId) {
        return delegate.findByPartIdAndRemovedFalse(partId);
    }

    @Override
    public Optional<PartStock> findByPartIdForUpdate(UUID partId) {
        return delegate.findByPartIdForUpdate(partId);
    }

    @Override
    public List<PartStock> findAll() {
        return delegate.findAllByRemovedFalse();
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<PartStock> existing = delegate.findByIdAndRemovedFalse(id);
        if (existing.isEmpty()) {
            return false;
        }
        PartStock entity = existing.get();
        entity.setRemoved(true);
        delegate.save(entity);
        return true;
    }
}
