package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.dto.request.BookTestDriveRequest;
import org.example.orderservice.application.dto.request.QuoteTestDriveRequest;
import org.example.orderservice.application.dto.response.TestDriveResponse;
import org.example.orderservice.application.mapper.TestDriveMapper;
import org.example.orderservice.application.port.out.CurrentUserProvider;
import org.example.orderservice.application.port.out.OrderWorkflowEventPublisher;
import org.example.orderservice.application.repository.TestDriveRepository;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.example.orderservice.domain.testdrive.model.TestDrive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestDriveService {

    private final TestDriveRepository testDriveRepository;
    private final CurrentUserProvider currentUserProvider;
    private final OrderWorkflowEventPublisher eventPublisher;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public TestDriveResponse bookTestDrive(BookTestDriveRequest request) {
        // Check there are no active bookings for this car at the same time
        List<TestDrive> conflicts = testDriveRepository
                .findByCarIdAndStartDateTime(request.carId(), request.startDateTime());
        if (!conflicts.isEmpty()) {
            throw new DomainValidationException(
                    "Car " + request.carId() + " already has an active test-drive booking at " + request.startDateTime()
            );
        }

        TestDrive testDrive = TestDriveMapper.INSTANCE.toTestDrive(request, currentUserProvider.getCurrentUserId());
        TestDrive saved = testDriveRepository.save(testDrive);
        return TestDriveMapper.INSTANCE.toTestDriveResponse(saved);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @testDriveSecurityEvaluator.isOwner(#testDriveId, authentication)")
    public TestDriveResponse getTestDrive(UUID testDriveId) {
        return TestDriveMapper.INSTANCE.toTestDriveResponse(
                testDriveRepository.findById(testDriveId)
                        .orElseThrow(() -> new EntityNotFoundException("TestDrive not found: " + testDriveId))
        );
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<TestDriveResponse> listTestDrives() {
        List<TestDrive> testDrives = currentUserProvider.hasElevatedReadAccess()
                ? testDriveRepository.findAll()
                : testDriveRepository.findAllByCustomerId(currentUserProvider.getCurrentUserId());

        return testDrives.stream()
                .map(TestDriveMapper.INSTANCE::toTestDriveResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @testDriveSecurityEvaluator.isOwner(#testDriveId, authentication)")
    public void deleteTestDrive(UUID testDriveId) {
        TestDrive testDrive = findTestDriveOrThrow(testDriveId);
        if (!testDriveRepository.deleteById(testDriveId)) {
            throw new EntityNotFoundException("TestDrive not found: " + testDriveId);
        }
        eventPublisher.publishTestDriveCancelled(testDrive.getId(), testDrive.getCustomerId());
    }

    // Status transitions

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TestDriveResponse quoteTestDrive(UUID testDriveId, QuoteTestDriveRequest request) {
        validateFee(request);
        TestDrive testDrive = findTestDriveOrThrow(testDriveId);
        if (testDrive.getStatus() != TestDriveStatus.PENDING) {
            throw new DomainValidationException(
                    "Can only quote a PENDING test drive, current status: " + testDrive.getStatus());
        }
        testDrive.setFee(request.fee());
        testDrive.setStatus(TestDriveStatus.WAITING_FOR_PAYMENT);
        TestDrive saved = testDriveRepository.save(testDrive);
        eventPublisher.publishTestDriveAwaitingPayment(saved.getId(), saved.getCustomerId(), saved.getFee());
        return TestDriveMapper.INSTANCE.toTestDriveResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TestDriveResponse confirmTestDrive(UUID testDriveId) {
        throw new DomainValidationException("Manual confirmation is retired; payment webhook confirms the test drive");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TestDriveResponse completeTestDrive(UUID testDriveId) {
        return transitionTestDrive(testDriveId, TestDriveStatus.CONFIRMED, TestDriveStatus.COMPLETED,
                "Can only complete a CONFIRMED test drive");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @testDriveSecurityEvaluator.isOwner(#testDriveId, authentication)")
    public TestDriveResponse cancelTestDrive(UUID testDriveId) {
        TestDrive testDrive = findTestDriveOrThrow(testDriveId);
        if (testDrive.getStatus() == TestDriveStatus.COMPLETED || testDrive.getStatus() == TestDriveStatus.CANCELLED) {
            throw new DomainValidationException("Cannot cancel a test drive with status: " + testDrive.getStatus());
        }
        testDrive.setStatus(TestDriveStatus.CANCELLED);
        TestDrive saved = testDriveRepository.save(testDrive);
        eventPublisher.publishTestDriveCancelled(saved.getId(), saved.getCustomerId());
        return TestDriveMapper.INSTANCE.toTestDriveResponse(saved);
    }

    @Transactional
    public void handlePaymentSucceeded(UUID testDriveId) {
        TestDrive testDrive = findTestDriveOrThrow(testDriveId);
        if (testDrive.getStatus() == TestDriveStatus.WAITING_FOR_PAYMENT) {
            testDrive.setStatus(TestDriveStatus.CONFIRMED);
            testDriveRepository.save(testDrive);
        }
    }

    @Transactional
    public void handlePaymentRefunded(UUID testDriveId) {
        TestDrive testDrive = findTestDriveOrThrow(testDriveId);
        if (testDrive.getStatus() != TestDriveStatus.COMPLETED) {
            testDrive.setStatus(TestDriveStatus.CANCELLED);
            TestDrive saved = testDriveRepository.save(testDrive);
            eventPublisher.publishTestDriveCancelled(saved.getId(), saved.getCustomerId());
        }
    }

    private TestDriveResponse transitionTestDrive(UUID testDriveId, TestDriveStatus required, TestDriveStatus next,
                                                  String errorMessage) {
        TestDrive testDrive = findTestDriveOrThrow(testDriveId);
        if (testDrive.getStatus() != required) {
            throw new DomainValidationException(errorMessage + ", current status: " + testDrive.getStatus());
        }
        testDrive.setStatus(next);
        return TestDriveMapper.INSTANCE.toTestDriveResponse(testDriveRepository.save(testDrive));
    }

    private TestDrive findTestDriveOrThrow(UUID testDriveId) {
        return testDriveRepository.findById(testDriveId)
                .orElseThrow(() -> new EntityNotFoundException("TestDrive not found: " + testDriveId));
    }

    private void validateFee(QuoteTestDriveRequest request) {
        BigDecimal fee = request == null ? null : request.fee();
        if (fee == null || fee.signum() <= 0) {
            throw new DomainValidationException("Test-drive fee must be greater than zero");
        }
    }
}
