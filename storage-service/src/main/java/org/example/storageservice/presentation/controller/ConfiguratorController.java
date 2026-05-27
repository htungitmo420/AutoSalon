package org.example.storageservice.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.CarConfigurationRequest;
import org.example.storageservice.application.dto.response.CarConfigurationResponse;
import org.example.storageservice.application.service.ConfiguratorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/configurator")
@Tag(name = "Configurator")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
public class ConfiguratorController {

	private final ConfiguratorService configuratorService;

	@PostMapping("/models/{modelId}")
	public CarConfigurationResponse buildConfiguration(@PathVariable UUID modelId,
													   @RequestBody CarConfigurationRequest request) {
		return configuratorService.buildConfiguration(modelId, request);
	}
}