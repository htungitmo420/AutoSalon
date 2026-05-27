package org.example.storageservice.infrastructure.jpa.repository;

import jakarta.persistence.LockModeType;
import org.example.storageservice.domain.stock.model.PartStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaPartStockRepository extends JpaRepository<PartStock, UUID> {

    Optional<PartStock> findByIdAndRemovedFalse(UUID id);

    Optional<PartStock> findByPartIdAndRemovedFalse(UUID partId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stock from PartStock stock where stock.partId = :partId and stock.removed = false")
    Optional<PartStock> findByPartIdForUpdate(@Param("partId") UUID partId);

    List<PartStock> findAllByRemovedFalse();
}
