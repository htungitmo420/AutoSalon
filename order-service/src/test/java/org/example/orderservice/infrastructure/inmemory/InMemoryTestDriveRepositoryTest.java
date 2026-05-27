package org.example.orderservice.infrastructure.inmemory;

import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.example.orderservice.domain.testdrive.model.TestDrive;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTestDriveRepositoryTest {

    private static final LocalDateTime FUTURE = LocalDateTime.now().plusDays(1);

    private TestDrive newTestDrive(UUID carId, UUID customerId, TestDriveStatus status, LocalDateTime start) {
        return TestDrive.builder()
                .carId(carId)
                .customerId(customerId)
                .status(status)
                .startDateTime(start)
                .build();
    }

    private TestDrive newPendingTestDrive() {
        return newTestDrive(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000001"), TestDriveStatus.PENDING, FUTURE);
    }

    @Test
    void save_assignsIdOnFirstSave() {
        var repo = new InMemoryTestDriveRepository();
        TestDrive testDrive = repo.save(newPendingTestDrive());
        assertNotNull(testDrive.getId());
    }

    @Test
    void save_nullThrowsIllegalArgument() {
        var repo = new InMemoryTestDriveRepository();
        assertThrows(IllegalArgumentException.class, () -> repo.save(null));
    }

    @Test
    void findById_returnsSavedTestDrive() {
        var repo = new InMemoryTestDriveRepository();
        TestDrive testDrive = repo.save(newPendingTestDrive());
        assertEquals(FUTURE, repo.findById(testDrive.getId()).orElseThrow().getStartDateTime());
    }

    @Test
    void save_updateExisting_noDuplicate() {
        var repo = new InMemoryTestDriveRepository();
        TestDrive testDrive = repo.save(newPendingTestDrive());
        testDrive.setStatus(TestDriveStatus.CONFIRMED);
        repo.save(testDrive);

        assertEquals(TestDriveStatus.CONFIRMED, repo.findById(testDrive.getId()).orElseThrow().getStatus());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void deleteById_removesExistingTestDrive() {
        var repo = new InMemoryTestDriveRepository();
        TestDrive testDrive = repo.save(newPendingTestDrive());
        assertTrue(repo.deleteById(testDrive.getId()));
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void deleteById_returnsFalseForMissingId() {
        var repo = new InMemoryTestDriveRepository();
        assertFalse(repo.deleteById(UUID.randomUUID()));
    }

    @Test
    void findByCarIdAndStartDateTime_returnsActiveConflicts() {
        var repo = new InMemoryTestDriveRepository();
        UUID carId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        repo.save(newPendingTestDrive());

        assertEquals(1, repo.findByCarIdAndStartDateTime(carId, FUTURE).size());
        assertEquals(0, repo.findByCarIdAndStartDateTime(carId, FUTURE.plusHours(1)).size());
        assertEquals(0, repo.findByCarIdAndStartDateTime(
                UUID.fromString("00000000-0000-0000-0000-000000000002"), FUTURE).size());
    }

    @Test
    void findByCarIdAndStartDateTime_excludesCancelledAndCompleted() {
        var repo = new InMemoryTestDriveRepository();
        UUID carId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        repo.save(newTestDrive(carId, UUID.fromString(
                "00000000-0000-0000-0000-000000000001"), TestDriveStatus.CANCELLED, FUTURE));
        repo.save(newTestDrive(carId, UUID.fromString(
                "00000000-0000-0000-0000-000000000002"), TestDriveStatus.COMPLETED, FUTURE));

        assertEquals(0, repo.findByCarIdAndStartDateTime(carId, FUTURE).size());
    }
}