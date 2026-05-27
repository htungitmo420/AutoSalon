package org.example.storageservice.infrastructure.jpa.repository;

import org.example.storageservice.domain.reservation.enums.ReservationStatus;
import org.example.storageservice.domain.reservation.model.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaInventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

    Optional<InventoryReservation> findByIdAndRemovedFalse(UUID id);

    Optional<InventoryReservation> findByOrderIdAndRemovedFalse(UUID orderId);

    List<InventoryReservation> findAllByRemovedFalse();

    List<InventoryReservation> findAllByCarIdAndRemovedFalse(UUID carId);

    List<InventoryReservation> findAllByStatusAndRemovedFalse(ReservationStatus status);
}
