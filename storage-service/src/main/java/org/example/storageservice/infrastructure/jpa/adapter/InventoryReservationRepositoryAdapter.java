package org.example.storageservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.repository.InventoryReservationRepository;
import org.example.storageservice.domain.reservation.enums.ReservationStatus;
import org.example.storageservice.domain.reservation.model.InventoryReservation;
import org.example.storageservice.infrastructure.jpa.repository.JpaInventoryReservationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InventoryReservationRepositoryAdapter implements InventoryReservationRepository {

    private final JpaInventoryReservationRepository delegate;

    @Override
    public InventoryReservation save(InventoryReservation entity) {
        return delegate.saveAndFlush(entity);
    }

    @Override
    public Optional<InventoryReservation> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public Optional<InventoryReservation> findByOrderId(UUID orderId) {
        return delegate.findByOrderIdAndRemovedFalse(orderId);
    }

    @Override
    public List<InventoryReservation> findAll() {
        return delegate.findAllByRemovedFalse();
    }

    @Override
    public List<InventoryReservation> findAllByCarId(UUID carId) {
        return delegate.findAllByCarIdAndRemovedFalse(carId);
    }

    @Override
    public List<InventoryReservation> findAllByStatus(ReservationStatus status) {
        return delegate.findAllByStatusAndRemovedFalse(status);
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<InventoryReservation> existing = delegate.findByIdAndRemovedFalse(id);
        if (existing.isEmpty()) {
            return false;
        }
        InventoryReservation entity = existing.get();
        entity.setRemoved(true);
        delegate.save(entity);
        return true;
    }
}
