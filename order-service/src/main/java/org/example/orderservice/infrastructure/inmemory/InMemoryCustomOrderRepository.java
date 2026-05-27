package org.example.orderservice.infrastructure.inmemory;

import org.example.orderservice.application.repository.CustomOrderRepository;
import org.example.orderservice.domain.order.model.CustomCarOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCustomOrderRepository implements CustomOrderRepository {

    private final ConcurrentHashMap<UUID, CustomCarOrder> storage = new ConcurrentHashMap<>();

    @Override
    public CustomCarOrder save(CustomCarOrder order) {
        if (order == null) throw new IllegalArgumentException("Custom order must not be null");
        if (order.getId() == null) {
            order.setId(UUID.randomUUID());
        }
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<CustomCarOrder> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<CustomCarOrder> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(UUID id) {
        return storage.remove(id) != null;
    }

    @Override
    public List<CustomCarOrder> findAllByCustomerId(UUID customerId) {
        return storage.values().stream()
                .filter(order -> customerId.equals(order.getCustomerId()))
                .toList();
    }

    @Override
    public boolean existsByIdAndCustomerId(UUID orderId, UUID customerId) {
        return findById(orderId)
                .map(order -> customerId.equals(order.getCustomerId()))
                .orElse(false);
    }
}