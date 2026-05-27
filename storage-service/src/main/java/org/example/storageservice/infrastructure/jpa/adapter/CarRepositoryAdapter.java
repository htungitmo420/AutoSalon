package org.example.storageservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.CarFilterRequest;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.domain.car.model.Car;
import org.example.storageservice.infrastructure.jpa.repository.JpaCarRepository;
import org.example.storageservice.infrastructure.jpa.specification.CarSpecifications;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CarRepositoryAdapter implements CarRepository {

    private final JpaCarRepository delegate;

    @Override
    public Car save(Car entity) {
        return delegate.save(entity);
    }

    @Override
    public Optional<Car> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public List<Car> findAll() {
        return delegate.findAllByRemovedFalse();
    }

    @Override
    public List<Car> findAllByFilter(CarFilterRequest filter) {
        return delegate.findAll(CarSpecifications.byFilter(filter));
    }

    @Override
    public List<Car> findAllForSale() {
        return delegate.findAllByRemovedFalseAndTestDriveFalse();
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<Car> existing = delegate.findByIdAndRemovedFalse(id);
        if (existing.isEmpty()) {
            return false;
        }
        Car entity = existing.get();
        entity.setRemoved(true);
        delegate.save(entity);
        return true;
    }
}