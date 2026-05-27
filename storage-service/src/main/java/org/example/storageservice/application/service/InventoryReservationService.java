package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.CarConfigurationRequest;
import org.example.storageservice.application.dto.response.AssemblyOrderResponse;
import org.example.storageservice.application.dto.response.CarConfigurationResponse;
import org.example.storageservice.application.mapper.InventoryReservationMapper;
import org.example.storageservice.application.port.out.InventoryReservationEventPublisher;
import org.example.storageservice.application.repository.AssemblyOrderRepository;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.application.repository.InventoryReservationRepository;
import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.reservation.enums.ReservationStatus;
import org.example.storageservice.domain.reservation.model.InventoryReservation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final InventoryReservationRepository inventoryReservationRepository;
    private final CarRepository carRepository;
    private final AssemblyOrderRepository assemblyOrderRepository;
    private final ConfiguratorService configuratorService;
    private final PartStockService partStockService;
    private final AssemblyOrderService assemblyOrderService;
    private final InventoryReservationEventPublisher eventPublisher;

    @Transactional
    public InventoryReservation reserveStockCar(UUID orderId, UUID carId, Instant expiresAt, String traceId) {
        validateExpiration(expiresAt);
        InventoryReservation existing = findExistingReservation(orderId);
        if (existing != null) {
            return validateRetry(existing, SourceOrderType.COMMON, carId, null);
        }

        var car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + carId));
        if (car.isTestDrive()) {
            throw new DomainValidationException("Test drive car cannot be reserved: " + carId);
        }

        expireStaleReservationsForCar(carId, traceId);
        boolean alreadyReserved = inventoryReservationRepository.findAllByCarId(carId).stream()
                .anyMatch(this::blocksStockCar);
        boolean legacyReservation = assemblyOrderRepository.findAllByCarId(carId).stream()
                .anyMatch(order -> order.getStatus() != AssemblyOrderStatus.FAIL);
        if (alreadyReserved || legacyReservation) {
            throw new DomainValidationException("Inventory unavailable for car: " + carId);
        }

        InventoryReservation reservation = InventoryReservationMapper.INSTANCE.toStockReservation(
                orderId, carId, car.getPrice(), expiresAt);
        return saveNewReservation(reservation);
    }

    @Transactional
    public InventoryReservation reserveConfiguration(UUID orderId, UUID modelId, Map<String, UUID> selectedPartIds,
                                                     Instant expiresAt, String traceId) {
        validateExpiration(expiresAt);
        InventoryReservation existing = findExistingReservation(orderId);
        if (existing != null) {
            return validateRetry(existing, SourceOrderType.CUSTOM, null, modelId);
        }

        Map<PartType, UUID> requestedParts = toTypedPartIds(selectedPartIds);
        CarConfigurationResponse configuration = configuratorService.buildConfiguration(
                modelId, new CarConfigurationRequest(requestedParts));
        Map<String, UUID> requiredParts = InventoryReservationMapper.INSTANCE.toRequiredPartIds(configuration);

        partStockService.reserveParts(requiredParts);

        InventoryReservation reservation = InventoryReservationMapper.INSTANCE.toConfigurationReservation(
                orderId, modelId, requiredParts, configuration.totalPrice(), expiresAt);
        return saveNewReservation(reservation);
    }

    @Transactional
    public InventoryReservation confirmReservation(UUID orderId, UUID reservationId, String traceId) {
        InventoryReservation reservation = findForOrder(orderId, reservationId);
        expireIfNeeded(reservation, traceId);
        if (reservation.getStatus() == ReservationStatus.CONFIRMED
                || reservation.getStatus() == ReservationStatus.FULFILLED) {
            return reservation;
        }
        if (reservation.getStatus() != ReservationStatus.HELD) {
            throw new DomainValidationException("Reservation cannot be confirmed in status: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(Instant.now());
        InventoryReservation saved = inventoryReservationRepository.save(reservation);

        if (saved.getSourceOrderType() == SourceOrderType.CUSTOM) {
            assemblyOrderService.createAssemblyOrder(
                    InventoryReservationMapper.INSTANCE.toAssemblyOrderRequest(saved));
        }
        return saved;
    }

    @Transactional
    public AssemblyOrderResponse assemble(UUID assemblyOrderId, String traceId) {
        AssemblyOrderResponse assemblyOrder = assemblyOrderService.getAssemblyOrder(assemblyOrderId);
        InventoryReservation reservation = findCustomReservation(assemblyOrder);
        if (reservation == null) {
            return assemblyOrderService.assemble(assemblyOrderId);
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new DomainValidationException("Only confirmed reservations can be assembled");
        }

        try {
            AssemblyOrderResponse assembled = assemblyOrderService.assembleReserved(assemblyOrderId);
            reservation.setStatus(ReservationStatus.FULFILLED);
            inventoryReservationRepository.save(reservation);
            eventPublisher.publishAssemblyCompleted(reservation, assemblyOrderId, traceId);
            return assembled;
        } catch (DomainValidationException ex) {
            AssemblyOrderResponse failed = assemblyOrderService.failToAssemble(assemblyOrderId, ex.getMessage());
            eventPublisher.publishAssemblyFailed(reservation, assemblyOrderId, ex.getMessage(), traceId);
            return failed;
        }
    }

    @Transactional
    public AssemblyOrderResponse failAssembly(UUID assemblyOrderId, String reason, String traceId) {
        AssemblyOrderResponse failed = assemblyOrderService.failToAssemble(assemblyOrderId, reason);
        InventoryReservation reservation = findCustomReservation(failed);
        if (reservation != null && reservation.getStatus() == ReservationStatus.CONFIRMED) {
            eventPublisher.publishAssemblyFailed(reservation, assemblyOrderId, reason, traceId);
        }
        return failed;
    }

    @Transactional
    public InventoryReservation releaseReservation(UUID orderId, UUID reservationId, String reason, String traceId) {
        InventoryReservation reservation = findForOrder(orderId, reservationId);
        if (reservation.getStatus() == ReservationStatus.RELEASED
                || reservation.getStatus() == ReservationStatus.EXPIRED) {
            return reservation;
        }
        if (reservation.getStatus() != ReservationStatus.HELD) {
            throw new DomainValidationException("Only held reservations can be released");
        }

        releaseHeldResources(reservation);
        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setReleaseReason(reason);
        return inventoryReservationRepository.save(reservation);
    }

    @Transactional
    public void expireHeldReservations() {
        String traceId = UUID.randomUUID().toString();
        inventoryReservationRepository.findAllByStatus(ReservationStatus.HELD).stream()
                .filter(reservation -> !reservation.getExpiresAt().isAfter(Instant.now()))
                .forEach(reservation -> expire(reservation, traceId));
    }

    private InventoryReservation findExistingReservation(UUID orderId) {
        return inventoryReservationRepository.findByOrderId(orderId).orElse(null);
    }

    private InventoryReservation findForOrder(UUID orderId, UUID reservationId) {
        InventoryReservation reservation = inventoryReservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + reservationId));
        if (!orderId.equals(reservation.getOrderId())) {
            throw new DomainValidationException("Reservation does not belong to order: " + orderId);
        }
        return reservation;
    }

    private InventoryReservation validateRetry(InventoryReservation reservation, SourceOrderType expectedType,
                                               UUID expectedCarId, UUID expectedModelId) {
        if (reservation.getSourceOrderType() != expectedType
                || !java.util.Objects.equals(reservation.getCarId(), expectedCarId)
                || !java.util.Objects.equals(reservation.getModelId(), expectedModelId)) {
            throw new DomainValidationException("Order already owns a different reservation");
        }
        if (reservation.getStatus() != ReservationStatus.HELD
                && reservation.getStatus() != ReservationStatus.CONFIRMED
                && reservation.getStatus() != ReservationStatus.FULFILLED) {
            throw new DomainValidationException("Order reservation is no longer active");
        }
        return reservation;
    }

    private void validateExpiration(Instant expiresAt) {
        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            throw new DomainValidationException("Reservation expiration must be in the future");
        }
    }

    private Map<PartType, UUID> toTypedPartIds(Map<String, UUID> selectedPartIds) {
        Map<PartType, UUID> typed = new HashMap<>();
        if (selectedPartIds == null) {
            return typed;
        }
        selectedPartIds.forEach((slot, partId) -> {
            try {
                typed.put(PartType.valueOf(slot), partId);
            } catch (IllegalArgumentException ex) {
                throw new DomainValidationException("Unknown part slot: " + slot);
            }
        });
        return typed;
    }

    private InventoryReservation saveNewReservation(InventoryReservation reservation) {
        try {
            return inventoryReservationRepository.save(reservation);
        } catch (DataIntegrityViolationException ex) {
            throw new DomainValidationException("Inventory is already reserved");
        }
    }

    private void expireStaleReservationsForCar(UUID carId, String traceId) {
        inventoryReservationRepository.findAllByCarId(carId).stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.HELD)
                .filter(reservation -> !reservation.getExpiresAt().isAfter(Instant.now()))
                .forEach(reservation -> expire(reservation, traceId));
    }

    private boolean blocksStockCar(InventoryReservation reservation) {
        return reservation.getStatus() == ReservationStatus.HELD
                || reservation.getStatus() == ReservationStatus.CONFIRMED
                || reservation.getStatus() == ReservationStatus.FULFILLED;
    }

    private void expireIfNeeded(InventoryReservation reservation, String traceId) {
        if (reservation.getStatus() == ReservationStatus.HELD
                && !reservation.getExpiresAt().isAfter(Instant.now())) {
            expire(reservation, traceId);
            throw new DomainValidationException("Reservation has expired");
        }
    }

    private void expire(InventoryReservation reservation, String traceId) {
        releaseHeldResources(reservation);
        reservation.setStatus(ReservationStatus.EXPIRED);
        reservation.setReleaseReason("Reservation expired");
        inventoryReservationRepository.save(reservation);
        eventPublisher.publishExpired(reservation, traceId);
    }

    private void releaseHeldResources(InventoryReservation reservation) {
        if (reservation.getSourceOrderType() == SourceOrderType.CUSTOM) {
            partStockService.releaseParts(reservation.getRequiredPartIds());
        }
    }

    private InventoryReservation findCustomReservation(AssemblyOrderResponse assemblyOrder) {
        InventoryReservation reservation = inventoryReservationRepository.findByOrderId(assemblyOrder.sourceOrderId())
                .orElse(null);
        if (reservation == null || reservation.getSourceOrderType() != SourceOrderType.CUSTOM) {
            return null;
        }
        return reservation;
    }
}
