package org.example.storageservice.infrastructure.inmemory;

import org.example.storageservice.application.repository.InventoryReservationRepository;
import org.example.storageservice.domain.reservation.enums.ReservationStatus;
import org.example.storageservice.domain.reservation.model.InventoryReservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInventoryReservationRepository implements InventoryReservationRepository {

    private final ConcurrentHashMap<UUID, InventoryReservation> storage = new ConcurrentHashMap<>();

    @Override
    public InventoryReservation save(InventoryReservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation must not be null");
        }
        if (reservation.getId() == null) {
            reservation.setId(UUID.randomUUID());
        }
        storage.put(reservation.getId(), reservation);
        return reservation;
    }

    @Override
    public Optional<InventoryReservation> findById(UUID id) {
        return Optional.ofNullable(storage.get(id)).filter(reservation -> !reservation.isRemoved());
    }

    @Override
    public Optional<InventoryReservation> findByOrderId(UUID orderId) {
        return storage.values().stream()
                .filter(reservation -> !reservation.isRemoved())
                .filter(reservation -> orderId.equals(reservation.getOrderId()))
                .findFirst();
    }

    @Override
    public List<InventoryReservation> findAll() {
        return storage.values().stream()
                .filter(reservation -> !reservation.isRemoved())
                .toList();
    }

    @Override
    public List<InventoryReservation> findAllByCarId(UUID carId) {
        return storage.values().stream()
                .filter(reservation -> !reservation.isRemoved())
                .filter(reservation -> carId.equals(reservation.getCarId()))
                .toList();
    }

    @Override
    public List<InventoryReservation> findAllByStatus(ReservationStatus status) {
        return storage.values().stream()
                .filter(reservation -> !reservation.isRemoved())
                .filter(reservation -> status == reservation.getStatus())
                .toList();
    }

    @Override
    public boolean deleteById(UUID id) {
        InventoryReservation existing = storage.get(id);
        if (existing == null || existing.isRemoved()) {
            return false;
        }
        existing.setRemoved(true);
        storage.put(id, existing);
        return true;
    }
}
