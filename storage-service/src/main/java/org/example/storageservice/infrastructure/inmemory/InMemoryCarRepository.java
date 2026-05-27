package org.example.storageservice.infrastructure.inmemory;

import org.example.storageservice.application.dto.request.CarFilterRequest;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.domain.car.model.Car;
import org.example.storageservice.domain.car.model.CarModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCarRepository implements CarRepository {

    private final ConcurrentHashMap<UUID, Car> storage = new ConcurrentHashMap<>();

    @Override
    public Car save(Car car) {
        if (car == null) throw new IllegalArgumentException("Car must not be null");
        if (car.getId() == null) {
            car.setId(UUID.randomUUID());
        }
        storage.put(car.getId(), car);
        return car;
    }

    @Override
    public Optional<Car> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Car> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Car> findAllByFilter(CarFilterRequest filter) {
        if (filter == null) {
            return findAll();
        }

        return storage.values().stream()
                .filter(car -> matchesFilter(car, filter))
                .toList();
    }

    @Override
    public List<Car> findAllForSale() {
        return storage.values().stream()
                .filter(car -> !car.isRemoved())
                .filter(car -> !car.isTestDrive())
                .toList();
    }

    @Override
    public boolean deleteById(UUID id) {
        return storage.remove(id) != null;
    }

    // Fallback for test in-memory
    private boolean matchesFilter(Car car, CarFilterRequest filter) {
        if (filter.minPrice() != null && car.getPrice().compareTo(BigDecimal.valueOf(filter.minPrice())) < 0) {
            return false;
        }
        if (filter.maxPrice() != null && car.getPrice().compareTo(BigDecimal.valueOf(filter.maxPrice())) > 0) {
            return false;
        }
        if (filter.color() != null && car.getColor() != filter.color()) {
            return false;
        }

        CarModel model = car.getModel();
        if (hasModelFilter(filter)) {
            if (model == null) {
                return false;
            }

            if (filter.brand() != null && model.getBrand() != filter.brand()) {
                return false;
            }
            if (filter.modelName() != null && filter.brand() != null
                    && !model.getModelName().equalsIgnoreCase(filter.modelName())) {
                return false;
            }
            if (filter.bodyType() != null && model.getBodyType() != filter.bodyType()) {
                return false;
            }
            if (filter.fuelType() != null && model.getFuelType() != filter.fuelType()) {
                return false;
            }
            if (filter.minEnginePower() != null && model.getEnginePower() < filter.minEnginePower()) {
                return false;
            }
            if (filter.maxEnginePower() != null && model.getEnginePower() > filter.maxEnginePower()) {
                return false;
            }
            if (filter.minEngineVolume() != null && model.getEngineVolumeLiters() < filter.minEngineVolume()) {
                return false;
            }
            if (filter.maxEngineVolume() != null && model.getEngineVolumeLiters() > filter.maxEngineVolume()) {
                return false;
            }
            if (filter.gearBoxType() != null && model.getGearBoxType() != filter.gearBoxType()) {
                return false;
            }
            if (filter.drivetrainType() != null && model.getDrivetrainType() != filter.drivetrainType()) {
                return false;
            }
        }

        if (hasComponentFilter(filter)) {
            if (model == null || model.getBasePartIds() == null) {
                return false;
            }
            if (!model.getBasePartIds().values().containsAll(filter.componentIds())) {
                return false;
            }
        }

        return true;
    }

    private boolean hasModelFilter(CarFilterRequest filter) {
        return filter.brand() != null
                || filter.modelName() != null
                || filter.bodyType() != null
                || filter.fuelType() != null
                || filter.minEnginePower() != null
                || filter.maxEnginePower() != null
                || filter.minEngineVolume() != null
                || filter.maxEngineVolume() != null
                || filter.gearBoxType() != null
                || filter.drivetrainType() != null;
    }

    private boolean hasComponentFilter(CarFilterRequest filter) {
        return filter.componentIds() != null && !filter.componentIds().isEmpty();
    }
}