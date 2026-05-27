package org.example.storageservice.infrastructure.inmemory;

import org.example.storageservice.application.repository.PartStockRepository;
import org.example.storageservice.domain.stock.model.PartStock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPartStockRepository implements PartStockRepository {

    private final ConcurrentHashMap<UUID, PartStock> storage = new ConcurrentHashMap<>();

    @Override
    public PartStock save(PartStock stock) {
        if (stock == null) throw new IllegalArgumentException("Part stock must not be null");
        if (stock.getId() == null) {
            stock.setId(UUID.randomUUID());
        }
        storage.put(stock.getId(), stock);
        return stock;
    }

    @Override
    public Optional<PartStock> findById(UUID id) {
        return Optional.ofNullable(storage.get(id))
                .filter(stock -> !stock.isRemoved());
    }

    @Override
    public Optional<PartStock> findByPartId(UUID partId) {
        return storage.values().stream()
                .filter(stock -> !stock.isRemoved())
                .filter(stock -> stock.getPartId().equals(partId))
                .findFirst();
    }

    @Override
    public Optional<PartStock> findByPartIdForUpdate(UUID partId) {
        return findByPartId(partId);
    }

    @Override
    public List<PartStock> findAll() {
        return storage.values().stream()
                .filter(stock -> !stock.isRemoved())
                .toList();
    }

    @Override
    public boolean deleteById(UUID id) {
        PartStock existing = storage.get(id);
        if (existing == null || existing.isRemoved()) {
            return false;
        }
        existing.setRemoved(true);
        storage.put(id, existing);
        return true;
    }
}
