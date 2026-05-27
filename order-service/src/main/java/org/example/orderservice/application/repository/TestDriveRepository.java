package org.example.orderservice.application.repository;

import org.example.orderservice.domain.testdrive.model.TestDrive;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TestDriveRepository extends Repository<TestDrive> {

    // Find all test drives for a given car that overlap with the specified time window.
    List<TestDrive> findByCarIdAndStartDateTime(UUID carId, LocalDateTime startDateTime);

    List<TestDrive> findAllByCustomerId(UUID customerId);

    boolean existsByIdAndCustomerId(UUID testDriveId, UUID customerId);
}