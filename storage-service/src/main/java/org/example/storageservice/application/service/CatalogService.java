package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.mapper.CarMapper;
import org.example.storageservice.application.dto.request.CarFilterRequest;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

// View a specific car.
// List cars in stock with optional filters.

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public CarResponse getCar(UUID carId) {
        return CarMapper.INSTANCE.toCarResponse(
                carRepository.findById(carId)
                        .orElseThrow(() -> new EntityNotFoundException("Car not found: " + carId))
        );
    }

    @Transactional(readOnly = true)
    public List<CarResponse> listCars(CarFilterRequest filter) {
        CarFilterRequest normalizedFilter = applyBusinessRules(filter);

        return carRepository.findAllByFilter(normalizedFilter).stream()
                .map(CarMapper.INSTANCE::toCarResponse)
                .toList();
    }

    private CarFilterRequest applyBusinessRules(CarFilterRequest filter) {
        if (filter == null) {
            return null;
        }

        return new CarFilterRequest(
                filter.minPrice(),
                filter.maxPrice(),
                filter.brand(),
                filter.modelName(),
                filter.bodyType(),
                filter.fuelType(),
                filter.minEnginePower(),
                filter.maxEnginePower(),
                filter.minEngineVolume(),
                filter.maxEngineVolume(),
                filter.gearBoxType(),
                filter.drivetrainType(),
                filter.color(),
                normalizeComponents(filter.componentIds())
        );
    }

    private Set<UUID> normalizeComponents(Set<UUID> componentIds) {
        if (componentIds == null || componentIds.isEmpty()) {
            return null;
        }
        return Set.copyOf(componentIds);
    }
}