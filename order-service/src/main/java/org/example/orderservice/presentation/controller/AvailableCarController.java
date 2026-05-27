package org.example.orderservice.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.dto.response.AvailableCarResponse;
import org.example.orderservice.application.service.AvailableCarQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cars")
@Tag(name = "Available Cars")
public class AvailableCarController {

    private final AvailableCarQueryService availableCarQueryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<AvailableCarResponse> listAvailableCars() {
        return availableCarQueryService.listAvailableCars();
    }

    @GetMapping("/{carId}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public AvailableCarResponse getAvailableCar(@PathVariable UUID carId) {
        return availableCarQueryService.getAvailableCar(carId);
    }
}
