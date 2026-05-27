package org.example.storageservice.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.service.PartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parts")
@Tag(name = "Parts")
@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
public class PartController {

	private final PartService partService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PartResponse create(@RequestBody SavePartRequest request) {
		return partService.createPart(request);
	}

	@GetMapping("/{partId}")
	public PartResponse getById(@PathVariable UUID partId) {
		return partService.getPart(partId);
	}

	@GetMapping
	public List<PartResponse> list() {
		return partService.listParts();
	}

	@PutMapping("/{partId}")
	public PartResponse update(@PathVariable UUID partId, @RequestBody SavePartRequest request) {
		return partService.updatePart(partId, request);
	}

	@DeleteMapping("/{partId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID partId) {
		partService.deletePart(partId);
	}
}