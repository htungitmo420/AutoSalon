package org.example.orderservice.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.dto.response.PageResponse;
import org.example.orderservice.application.dto.response.TestDriveResponse;
import org.example.orderservice.application.dto.request.QuoteTestDriveRequest;
import org.example.orderservice.application.service.AdminTestDriveQueryService;
import org.example.orderservice.application.service.TestDriveService;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/test-drives")
@Tag(name = "Admin Test Drives", description = "Test-drive administration and status transitions")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class AdminTestDriveController {

    private final AdminTestDriveQueryService queryService;
    private final TestDriveService testDriveService;

    @GetMapping
    @Operation(summary = "List test drives with paging and filters")
    public PageResponse<TestDriveResponse> list(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID carId,
            @RequestParam(required = false) TestDriveStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDateTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return queryService.listTestDrives(customerId, carId, status, page, size, sortBy, sortDirection);
    }

    @GetMapping("/{testDriveId}")
    public TestDriveResponse getById(@PathVariable UUID testDriveId) {
        return testDriveService.getTestDrive(testDriveId);
    }

    @PatchMapping("/{testDriveId}/confirm")
    public TestDriveResponse confirm(@PathVariable UUID testDriveId) {
        return testDriveService.confirmTestDrive(testDriveId);
    }

    @PatchMapping("/{testDriveId}/quote")
    public TestDriveResponse quote(@PathVariable UUID testDriveId, @RequestBody QuoteTestDriveRequest request) {
        return testDriveService.quoteTestDrive(testDriveId, request);
    }

    @PatchMapping("/{testDriveId}/complete")
    public TestDriveResponse complete(@PathVariable UUID testDriveId) {
        return testDriveService.completeTestDrive(testDriveId);
    }

    @PatchMapping("/{testDriveId}/cancel")
    public TestDriveResponse cancel(@PathVariable UUID testDriveId) {
        return testDriveService.cancelTestDrive(testDriveId);
    }

    @DeleteMapping("/{testDriveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID testDriveId) {
        testDriveService.deleteTestDrive(testDriveId);
    }
}
