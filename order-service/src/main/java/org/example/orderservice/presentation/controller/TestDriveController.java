package org.example.orderservice.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.dto.request.BookTestDriveRequest;
import org.example.orderservice.application.dto.response.TestDriveResponse;
import org.example.orderservice.application.service.TestDriveService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test-drives")
@Tag(name = "Test Drives")
public class TestDriveController {

	private final TestDriveService testDriveService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public TestDriveResponse book(@RequestBody BookTestDriveRequest request) {
		return testDriveService.bookTestDrive(request);
	}

	@GetMapping("/{testDriveId}")
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public TestDriveResponse getById(@PathVariable UUID testDriveId) {
		return testDriveService.getTestDrive(testDriveId);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public List<TestDriveResponse> list() {
		return testDriveService.listTestDrives();
	}

	@DeleteMapping("/{testDriveId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public void delete(@PathVariable UUID testDriveId) {
		testDriveService.deleteTestDrive(testDriveId);
	}

	@PatchMapping("/{testDriveId}/confirm")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public TestDriveResponse confirm(@PathVariable UUID testDriveId) {
		return testDriveService.confirmTestDrive(testDriveId);
	}

	@PatchMapping("/{testDriveId}/complete")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public TestDriveResponse complete(@PathVariable UUID testDriveId) {
		return testDriveService.completeTestDrive(testDriveId);
	}

	@PatchMapping("/{testDriveId}/cancel")
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public TestDriveResponse cancel(@PathVariable UUID testDriveId) {
		return testDriveService.cancelTestDrive(testDriveId);
	}
}