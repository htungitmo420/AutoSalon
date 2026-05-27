package org.example.storageservice.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.service.CarModelService;
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
@RequestMapping("/api/models")
@Tag(name = "Car Models")
@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
public class CarModelController {

	private final CarModelService carModelService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CarModelResponse create(@RequestBody SaveCarModelRequest request) {
		return carModelService.createCarModel(request);
	}

	@GetMapping("/{modelId}")
	public CarModelResponse getById(@PathVariable UUID modelId) {
		return carModelService.getCarModel(modelId);
	}

	@GetMapping
	public List<CarModelResponse> list() {
		return carModelService.listCarModels();
	}

	@PutMapping("/{modelId}")
	public CarModelResponse update(@PathVariable UUID modelId, @RequestBody SaveCarModelRequest request) {
		return carModelService.updateCarModel(modelId, request);
	}

	@DeleteMapping("/{modelId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID modelId) {
		carModelService.deleteCarModel(modelId);
	}
}