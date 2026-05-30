package org.example.storageservice.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.CarFilterRequest;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/catalog", "/api/v1/catalog"})
@Tag(name = "Catalog")
public class CatalogController {

	private final CatalogService catalogService;

	@GetMapping("/cars/{carId}")
	public CarResponse getCar(@PathVariable UUID carId) {
		return catalogService.getCar(carId);
	}

	@PostMapping("/cars/filter")
	public List<CarResponse> filterCars(@RequestBody(required = false) CarFilterRequest filter) {
		return catalogService.listCars(filter);
	}

	@GetMapping("/models")
	public List<CarModelResponse> listModels() {
		return catalogService.listModels();
	}

	@GetMapping("/models/{modelId}")
	public CarModelResponse getModel(@PathVariable UUID modelId) {
		return catalogService.getModel(modelId);
	}

	@GetMapping("/parts")
	public List<PartResponse> listParts() {
		return catalogService.listParts();
	}

	@GetMapping("/parts/{partId}")
	public PartResponse getPart(@PathVariable UUID partId) {
		return catalogService.getPart(partId);
	}
}
