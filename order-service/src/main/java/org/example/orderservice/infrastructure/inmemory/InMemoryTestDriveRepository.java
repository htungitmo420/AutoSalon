package org.example.orderservice.infrastructure.inmemory;

import org.example.orderservice.application.repository.TestDriveRepository;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.example.orderservice.domain.testdrive.model.TestDrive;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryTestDriveRepository implements TestDriveRepository {

    private final ConcurrentHashMap<UUID, TestDrive> storage = new ConcurrentHashMap<>();

    @Override
    public TestDrive save(TestDrive request) {
        if (request == null) throw new IllegalArgumentException("Request must not be null");
        if (request.getId() == null) {
            request.setId(UUID.randomUUID());
        }
        storage.put(request.getId(), request);
        return request;
    }

    @Override
    public Optional<TestDrive> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<TestDrive> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(UUID id) {
        return storage.remove(id) != null;
    }

    @Override
    public List<TestDrive> findByCarIdAndStartDateTime(UUID carId, LocalDateTime startDateTime) {
        return storage.values().stream()
                .filter(td -> td.getCarId().equals(carId)
                        && td.getStartDateTime().equals(startDateTime)
                        && td.getStatus() != TestDriveStatus.CANCELLED
                        && td.getStatus() != TestDriveStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestDrive> findAllByCustomerId(UUID customerId) {
        return storage.values().stream()
                .filter(td -> customerId.equals(td.getCustomerId()))
                .toList();
    }

    @Override
    public boolean existsByIdAndCustomerId(UUID testDriveId, UUID customerId) {
        return findById(testDriveId)
                .map(td -> customerId.equals(td.getCustomerId()))
                .orElse(false);
    }
}