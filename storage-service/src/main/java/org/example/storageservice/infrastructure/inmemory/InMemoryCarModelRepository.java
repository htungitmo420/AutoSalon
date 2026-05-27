package org.example.storageservice.infrastructure.inmemory;

import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.domain.car.model.CarModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCarModelRepository implements CarModelRepository {

    private final ConcurrentHashMap<UUID, CarModel> storage = new ConcurrentHashMap<>();

    @Override
    public CarModel save(CarModel model) {
        if (model == null) throw new IllegalArgumentException("Model must not be null");
        if (model.getId() == null) {
            model.setId(UUID.randomUUID());
        }
        storage.put(model.getId(), model);
        return model;
    }

    @Override
    public Optional<CarModel> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<CarModel> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(UUID id) {
        return storage.remove(id) != null;
    }
}