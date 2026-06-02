package org.example.orderservice.infrastructure.inmemory;

import org.example.orderservice.application.repository.CommonOrderRepository;
import org.example.orderservice.domain.order.model.CommonCarOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCommonOrderRepository implements CommonOrderRepository {

    private final ConcurrentHashMap<UUID, CommonCarOrder> storage = new ConcurrentHashMap<>();

    @Override
    public CommonCarOrder save(CommonCarOrder order) {
        if (order == null) throw new IllegalArgumentException("Common order must not be null");
        if (order.getId() == null) {
            order.setId(UUID.randomUUID());
        }
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<CommonCarOrder> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<CommonCarOrder> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(UUID id) {
        return storage.remove(id) != null;
    }

    @Override
    public List<CommonCarOrder> findAllByCustomerId(UUID customerId) {
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

    @Override
    public Optional<CommonCarOrder> findByCartId(UUID cartId) {
        return storage.values().stream()
                .filter(order -> cartId.equals(order.getCartId()))
                .findFirst();
    }
}
