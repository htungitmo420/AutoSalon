package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.response.AvailableCarResponse;
import org.example.storageservice.application.mapper.CarMapper;
import org.example.storageservice.application.repository.AssemblyOrderRepository;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.application.repository.InventoryReservationRepository;
import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.model.AssemblyOrder;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.reservation.enums.ReservationStatus;
import org.example.storageservice.domain.reservation.model.InventoryReservation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailableCarService {

    private final CarRepository carRepository;
    private final AssemblyOrderRepository assemblyOrderRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    @Transactional
    public List<AvailableCarResponse> listAvailableCars() {
        Set<UUID> reservedCarIds = findReservedCarIds();

        return carRepository.findAllForSale().stream()
                .filter(car -> !reservedCarIds.contains(car.getId()))
                .map(CarMapper.INSTANCE::toAvailableCarResponse)
                .toList();
    }

    @Transactional
    public AvailableCarResponse getAvailableCar(UUID carId) {
        Set<UUID> reservedCarIds = findReservedCarIds();

        return carRepository.findById(carId)
                .filter(car -> !car.isTestDrive())
                .filter(car -> !reservedCarIds.contains(car.getId()))
                .map(CarMapper.INSTANCE::toAvailableCarResponse)
                .orElseThrow(() -> new EntityNotFoundException("Available car not found: " + carId));
    }

    private Set<UUID> findReservedCarIds() {
        Set<UUID> reservedCarIds = assemblyOrderRepository.findAll().stream()
                .filter(order -> order.getCarId() != null)
                .filter(order -> order.getStatus() != AssemblyOrderStatus.FAIL)
                .map(AssemblyOrder::getCarId)
                .collect(Collectors.toSet());
        inventoryReservationRepository.findAll().stream()
                .filter(reservation -> reservation.getCarId() != null)
                .filter(this::isActiveReservation)
                .map(InventoryReservation::getCarId)
                .forEach(reservedCarIds::add);
        return reservedCarIds;
    }

    private boolean isActiveReservation(InventoryReservation reservation) {
        return reservation.getStatus() == ReservationStatus.HELD
                || reservation.getStatus() == ReservationStatus.CONFIRMED
                || reservation.getStatus() == ReservationStatus.FULFILLED;
    }
}
