package org.example.storageservice.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.dto.response.InventoryReservationResponse;
import org.example.storageservice.application.dto.response.PageResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.dto.response.PartStockResponse;
import org.example.storageservice.application.service.AdminInventoryQueryService;
import org.example.storageservice.application.service.CarModelService;
import org.example.storageservice.application.service.CarService;
import org.example.storageservice.application.service.PartService;
import org.example.storageservice.application.service.PartStockService;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.reservation.enums.ReservationStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/inventory")
@Tag(name = "Admin Inventory", description = "Warehouse catalog, stock and reservation administration")
@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
public class AdminInventoryController {

    private final AdminInventoryQueryService queryService;
    private final CarService carService;
    private final CarModelService carModelService;
    private final PartService partService;
    private final PartStockService partStockService;

    @GetMapping("/cars")
    @Operation(summary = "List cars with paging and filters")
    public PageResponse<CarResponse> listCars(
            @RequestParam(required = false) UUID modelId,
            @RequestParam(required = false) Color color,
            @RequestParam(required = false) Boolean testDrive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return queryService.listCars(modelId, color, testDrive, page, size, sortBy, sortDirection);
    }

    @PostMapping("/cars")
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse createCar(@RequestBody SaveCarRequest request) {
        return carService.createCar(request);
    }

    @GetMapping("/cars/{carId}")
    public CarResponse getCar(@PathVariable UUID carId) {
        return carService.getCar(carId);
    }

    @PutMapping("/cars/{carId}")
    public CarResponse updateCar(@PathVariable UUID carId, @RequestBody SaveCarRequest request) {
        return carService.updateCar(carId, request);
    }

    @DeleteMapping("/cars/{carId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable UUID carId) {
        carService.deleteCar(carId);
    }

    @GetMapping("/models")
    @Operation(summary = "List car models with paging and filters")
    public PageResponse<CarModelResponse> listModels(
            @RequestParam(required = false) Brand brand,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "modelName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return queryService.listModels(brand, search, page, size, sortBy, sortDirection);
    }

    @PostMapping("/models")
    @ResponseStatus(HttpStatus.CREATED)
    public CarModelResponse createModel(@RequestBody SaveCarModelRequest request) {
        return carModelService.createCarModel(request);
    }

    @GetMapping("/models/{modelId}")
    public CarModelResponse getModel(@PathVariable UUID modelId) {
        return carModelService.getCarModel(modelId);
    }

    @PutMapping("/models/{modelId}")
    public CarModelResponse updateModel(@PathVariable UUID modelId, @RequestBody SaveCarModelRequest request) {
        return carModelService.updateCarModel(modelId, request);
    }

    @DeleteMapping("/models/{modelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModel(@PathVariable UUID modelId) {
        carModelService.deleteCarModel(modelId);
    }

    @GetMapping("/parts")
    @Operation(summary = "List parts with paging and filters")
    public PageResponse<PartResponse> listParts(
            @RequestParam(required = false) PartType type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return queryService.listParts(type, search, page, size, sortBy, sortDirection);
    }

    @PostMapping("/parts")
    @ResponseStatus(HttpStatus.CREATED)
    public PartResponse createPart(@RequestBody SavePartRequest request) {
        return partService.createPart(request);
    }

    @GetMapping("/parts/{partId}")
    public PartResponse getPart(@PathVariable UUID partId) {
        return partService.getPart(partId);
    }

    @PutMapping("/parts/{partId}")
    public PartResponse updatePart(@PathVariable UUID partId, @RequestBody SavePartRequest request) {
        return partService.updatePart(partId, request);
    }

    @DeleteMapping("/parts/{partId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePart(@PathVariable UUID partId) {
        partService.deletePart(partId);
    }

    @GetMapping("/stocks")
    @Operation(summary = "List stock levels with paging and low-stock filter")
    public PageResponse<PartStockResponse> listStocks(
            @RequestParam(required = false) UUID partId,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(defaultValue = "5") int threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "availableQuantity") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return queryService.listStocks(partId, lowStock, threshold, page, size, sortBy, sortDirection);
    }

    @PostMapping("/stocks")
    @ResponseStatus(HttpStatus.CREATED)
    public PartStockResponse createStock(@RequestBody SavePartStockRequest request) {
        return partStockService.createPartStock(request);
    }

    @GetMapping("/stocks/{stockId}")
    public PartStockResponse getStock(@PathVariable UUID stockId) {
        return partStockService.getPartStock(stockId);
    }

    @PutMapping("/stocks/{stockId}")
    public PartStockResponse updateStock(@PathVariable UUID stockId, @RequestBody SavePartStockRequest request) {
        return partStockService.updatePartStock(stockId, request);
    }

    @DeleteMapping("/stocks/{stockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStock(@PathVariable UUID stockId) {
        partStockService.deletePartStock(stockId);
    }

    @GetMapping("/reservations")
    @Operation(summary = "List reservation holds with status and ownership filters")
    public PageResponse<InventoryReservationResponse> listReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) UUID carId,
            @RequestParam(required = false) UUID modelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return queryService.listReservations(status, orderId, carId, modelId, page, size, sortBy, sortDirection);
    }

    @GetMapping("/reservations/{reservationId}")
    public InventoryReservationResponse getReservation(@PathVariable UUID reservationId) {
        return queryService.getReservation(reservationId);
    }
}
