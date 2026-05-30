package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.dto.response.PageResponse;
import org.example.orderservice.application.dto.response.TestDriveResponse;
import org.example.orderservice.application.mapper.TestDriveMapper;
import org.example.orderservice.application.repository.TestDriveRepository;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.example.orderservice.domain.testdrive.model.TestDrive;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminTestDriveQueryService {

    private final TestDriveRepository testDriveRepository;

    @Transactional(readOnly = true)
    public PageResponse<TestDriveResponse> listTestDrives(UUID customerId, UUID carId, TestDriveStatus status,
                                                          int page, int size, String sortBy, String sortDirection) {
        Stream<TestDrive> testDrives = testDriveRepository.findAll().stream()
                .filter(testDrive -> customerId == null || customerId.equals(testDrive.getCustomerId()))
                .filter(testDrive -> carId == null || carId.equals(testDrive.getCarId()))
                .filter(testDrive -> status == null || status == testDrive.getStatus());
        return PageSupport.page(testDrives, page, size, sortBy, sortDirection, comparator(sortBy),
                TestDriveMapper.INSTANCE::toTestDriveResponse);
    }

    private Comparator<TestDrive> comparator(String sortBy) {
        return switch (sortBy) {
            case "startDateTime" -> Comparator.comparing(
                    TestDrive::getStartDateTime, Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdAt" -> Comparator.comparing(TestDrive::getCreatedAt);
            case "status" -> Comparator.comparing(TestDrive::getStatus);
            case "id" -> Comparator.comparing(TestDrive::getId);
            default -> throw new DomainValidationException("Unsupported sort field for test drives: " + sortBy);
        };
    }
}
