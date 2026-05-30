package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.dto.response.InventoryReservationResponse;
import org.example.storageservice.application.dto.response.PageResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.dto.response.PartStockResponse;
import org.example.storageservice.application.mapper.CarMapper;
import org.example.storageservice.application.mapper.InventoryReservationMapper;
import org.example.storageservice.application.mapper.PartMapper;
import org.example.storageservice.application.mapper.PartStockMapper;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.application.repository.InventoryReservationRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.application.repository.PartStockRepository;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.car.model.Car;
import org.example.storageservice.domain.car.model.CarModel;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.part.models.Part;
import org.example.storageservice.domain.reservation.enums.ReservationStatus;
import org.example.storageservice.domain.reservation.model.InventoryReservation;
import org.example.storageservice.domain.stock.model.PartStock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminInventoryQueryService {

    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final PartRepository partRepository;
    private final PartStockRepository partStockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    @Transactional(readOnly = true)
    public PageResponse<CarResponse> listCars(UUID modelId, Color color, Boolean testDrive, int page, int size,
                                              String sortBy, String sortDirection) {
        Stream<Car> cars = carRepository.findAll().stream()
                .filter(car -> modelId == null || modelId.equals(car.getModelId()))
                .filter(car -> color == null || color == car.getColor())
                .filter(car -> testDrive == null || testDrive == car.isTestDrive());
        return PageSupport.page(cars, page, size, sortBy, sortDirection, carComparator(sortBy),
                CarMapper.INSTANCE::toCarResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<CarModelResponse> listModels(Brand brand, String search, int page, int size,
                                                     String sortBy, String sortDirection) {
        Stream<CarModel> models = carModelRepository.findAll().stream()
                .filter(model -> brand == null || brand == model.getBrand())
                .filter(model -> containsIgnoreCase(model.getModelName(), search));
        return PageSupport.page(models, page, size, sortBy, sortDirection, modelComparator(sortBy),
                CarMapper.INSTANCE::toCarModelResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<PartResponse> listParts(PartType type, String search, int page, int size,
                                                String sortBy, String sortDirection) {
        Stream<Part> parts = partRepository.findAll().stream()
                .filter(part -> type == null || type == part.getType())
                .filter(part -> containsIgnoreCase(part.getName(), search));
        return PageSupport.page(parts, page, size, sortBy, sortDirection, partComparator(sortBy),
                PartMapper.INSTANCE::toPartResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<PartStockResponse> listStocks(UUID partId, Boolean lowStock, int threshold, int page, int size,
                                                      String sortBy, String sortDirection) {
        Stream<PartStock> stocks = partStockRepository.findAll().stream()
                .filter(stock -> partId == null || partId.equals(stock.getPartId()))
                .filter(stock -> lowStock == null || !lowStock || stock.getAvailableQuantity() <= threshold);
        return PageSupport.page(stocks, page, size, sortBy, sortDirection, stockComparator(sortBy),
                PartStockMapper.INSTANCE::toPartStockResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryReservationResponse> listReservations(ReservationStatus status, UUID orderId,
                                                                       UUID carId, UUID modelId, int page, int size,
                                                                       String sortBy, String sortDirection) {
        Stream<InventoryReservation> reservations = inventoryReservationRepository.findAll().stream()
                .filter(reservation -> status == null || status == reservation.getStatus())
                .filter(reservation -> orderId == null || orderId.equals(reservation.getOrderId()))
                .filter(reservation -> carId == null || carId.equals(reservation.getCarId()))
                .filter(reservation -> modelId == null || modelId.equals(reservation.getModelId()));
        return PageSupport.page(reservations, page, size, sortBy, sortDirection, reservationComparator(sortBy),
                InventoryReservationMapper.INSTANCE::toInventoryReservationResponse);
    }

    @Transactional(readOnly = true)
    public InventoryReservationResponse getReservation(UUID reservationId) {
        return InventoryReservationMapper.INSTANCE.toInventoryReservationResponse(
                inventoryReservationRepository.findById(reservationId)
                        .orElseThrow(() -> new org.example.storageservice.domain.exceptions.EntityNotFoundException(
                                "Reservation not found: " + reservationId)));
    }

    private Comparator<Car> carComparator(String sortBy) {
        return switch (sortBy) {
            case "price" -> Comparator.comparing(Car::getPrice);
            case "createdAt" -> Comparator.comparing(Car::getCreatedAt);
            case "id" -> Comparator.comparing(Car::getId);
            default -> throw unsupportedSort(sortBy, "cars");
        };
    }

    private Comparator<CarModel> modelComparator(String sortBy) {
        return switch (sortBy) {
            case "modelName" -> Comparator.comparing(CarModel::getModelName, String.CASE_INSENSITIVE_ORDER);
            case "basePrice" -> Comparator.comparing(CarModel::getBasePrice);
            case "createdAt" -> Comparator.comparing(CarModel::getCreatedAt);
            case "id" -> Comparator.comparing(CarModel::getId);
            default -> throw unsupportedSort(sortBy, "models");
        };
    }

    private Comparator<Part> partComparator(String sortBy) {
        return switch (sortBy) {
            case "name" -> Comparator.comparing(Part::getName, String.CASE_INSENSITIVE_ORDER);
            case "surcharge" -> Comparator.comparing(Part::getSurcharge);
            case "createdAt" -> Comparator.comparing(Part::getCreatedAt);
            case "id" -> Comparator.comparing(Part::getId);
            default -> throw unsupportedSort(sortBy, "parts");
        };
    }

    private Comparator<PartStock> stockComparator(String sortBy) {
        return switch (sortBy) {
            case "quantity" -> Comparator.comparingInt(PartStock::getQuantity);
            case "reservedQuantity" -> Comparator.comparingInt(PartStock::getReservedQuantity);
            case "availableQuantity" -> Comparator.comparingInt(PartStock::getAvailableQuantity);
            case "id" -> Comparator.comparing(PartStock::getId);
            default -> throw unsupportedSort(sortBy, "stocks");
        };
    }

    private Comparator<InventoryReservation> reservationComparator(String sortBy) {
        return switch (sortBy) {
            case "expiresAt" -> Comparator.comparing(InventoryReservation::getExpiresAt);
            case "createdAt" -> Comparator.comparing(InventoryReservation::getCreatedAt);
            case "status" -> Comparator.comparing(InventoryReservation::getStatus);
            case "id" -> Comparator.comparing(InventoryReservation::getId);
            default -> throw unsupportedSort(sortBy, "reservations");
        };
    }

    private boolean containsIgnoreCase(String value, String search) {
        return search == null || search.isBlank()
                || value.toLowerCase(Locale.ROOT).contains(search.trim().toLowerCase(Locale.ROOT));
    }

    private DomainValidationException unsupportedSort(String sortBy, String resource) {
        return new DomainValidationException("Unsupported sort field for " + resource + ": " + sortBy);
    }
}
