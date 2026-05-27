package org.example.storageservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.domain.car.model.CarModel;
import org.example.storageservice.infrastructure.jpa.repository.JpaCarModelRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CarModelRepositoryAdapter implements CarModelRepository {

    private final JpaCarModelRepository delegate;

    @Override
    public CarModel save(CarModel entity) {
        return delegate.save(entity);
    }

    @Override
    public Optional<CarModel> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public List<CarModel> findAll() {
        return delegate.findAllByRemovedFalse();
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<CarModel> existing = delegate.findByIdAndRemovedFalse(id);
        if (existing.isEmpty()) {
            return false;
        }
        CarModel entity = existing.get();
        entity.setRemoved(true);
        delegate.save(entity);
        return true;
    }
}