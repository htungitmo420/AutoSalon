package org.example.orderservice.infrastructure.jpa.repository;

import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.example.orderservice.domain.testdrive.model.TestDrive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaTestDriveRepository extends JpaRepository<TestDrive, UUID> {

    Optional<TestDrive> findByIdAndRemovedFalse(UUID id);

    List<TestDrive> findAllByRemovedFalse();

    List<TestDrive> findByCarIdAndStartDateTimeAndStatusNotInAndRemovedFalse(UUID carId, LocalDateTime startDateTime,
            Collection<TestDriveStatus> statuses);

    List<TestDrive> findAllByCustomerIdAndRemovedFalse(UUID customerId);

    boolean existsByIdAndCustomerIdAndRemovedFalse(UUID id, UUID customerId);
}