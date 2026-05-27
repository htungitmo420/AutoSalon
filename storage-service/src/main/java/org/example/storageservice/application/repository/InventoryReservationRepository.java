package org.example.storageservice.application.repository;

import org.example.storageservice.domain.reservation.enums.ReservationStatus;
import org.example.storageservice.domain.reservation.model.InventoryReservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationRepository extends Repository<InventoryReservation> {

    Optional<InventoryReservation> findByOrderId(UUID orderId);

    List<InventoryReservation> findAllByCarId(UUID carId);

    List<InventoryReservation> findAllByStatus(ReservationStatus status);
}
