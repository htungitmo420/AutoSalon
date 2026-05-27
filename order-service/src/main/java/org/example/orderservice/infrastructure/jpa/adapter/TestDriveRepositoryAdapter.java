package org.example.orderservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.repository.TestDriveRepository;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.example.orderservice.domain.testdrive.model.TestDrive;
import org.example.orderservice.infrastructure.jpa.repository.JpaTestDriveRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TestDriveRepositoryAdapter implements TestDriveRepository {

    private final JpaTestDriveRepository delegate;

    @Override
    public TestDrive save(TestDrive entity) {
        return delegate.save(entity);
    }

    @Override
    public Optional<TestDrive> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public List<TestDrive> findAll() {
        return delegate.findAllByRemovedFalse();
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<TestDrive> existing = delegate.findByIdAndRemovedFalse(id);
        if (existing.isEmpty()) {
            return false;
        }
        TestDrive entity = existing.get();
        entity.setRemoved(true);
        delegate.save(entity);
        return true;
    }

    @Override
    public List<TestDrive> findByCarIdAndStartDateTime(UUID carId, LocalDateTime startDateTime) {
        return delegate.findByCarIdAndStartDateTimeAndStatusNotInAndRemovedFalse(
                carId,
                startDateTime,
                EnumSet.of(TestDriveStatus.CANCELLED, TestDriveStatus.COMPLETED)
        );
    }

    @Override
    public List<TestDrive> findAllByCustomerId(UUID customerId) {
        return delegate.findAllByCustomerIdAndRemovedFalse(customerId);
    }

    @Override
    public boolean existsByIdAndCustomerId(UUID testDriveId, UUID customerId) {
        return delegate.existsByIdAndCustomerIdAndRemovedFalse(testDriveId, customerId);
    }
}