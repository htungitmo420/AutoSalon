package org.example.storageservice.infrastructure.inmemory;

import org.example.storageservice.application.repository.AssemblyOrderRepository;
import org.example.storageservice.domain.assembly.model.AssemblyOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAssemblyOrderRepository implements AssemblyOrderRepository {

    private final ConcurrentHashMap<UUID, AssemblyOrder> storage = new ConcurrentHashMap<>();

    @Override
    public AssemblyOrder save(AssemblyOrder order) {
        if (order == null) throw new IllegalArgumentException("Assembly order must not be null");
        if (order.getId() == null) {
            order.setId(UUID.randomUUID());
        }
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<AssemblyOrder> findById(UUID id) {
        return Optional.ofNullable(storage.get(id))
                .filter(order -> !order.isRemoved());
    }

    @Override
    public List<AssemblyOrder> findAll() {
        return storage.values().stream()
                .filter(order -> !order.isRemoved())
                .toList();
    }

    @Override
    public List<AssemblyOrder> findAllBySourceOrderId(UUID sourceOrderId) {
        if (sourceOrderId == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(order -> !order.isRemoved())
                .filter(order -> sourceOrderId.equals(order.getSourceOrderId()))
                .toList();
    }

    @Override
    public List<AssemblyOrder> findAllByCarId(UUID carId) {
        if (carId == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(order -> !order.isRemoved())
                .filter(order -> carId.equals(order.getCarId()))
                .toList();
    }

    @Override
    public boolean deleteById(UUID id) {
        AssemblyOrder existing = storage.get(id);
        if (existing == null || existing.isRemoved()) {
            return false;
        }
        existing.setRemoved(true);
        storage.put(id, existing);
        return true;
    }
}
