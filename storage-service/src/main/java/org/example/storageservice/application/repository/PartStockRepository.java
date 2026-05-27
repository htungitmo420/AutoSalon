package org.example.storageservice.application.repository;

import org.example.storageservice.domain.stock.model.PartStock;

import java.util.Optional;
import java.util.UUID;

public interface PartStockRepository extends Repository<PartStock> {

    Optional<PartStock> findByPartId(UUID partId);

    Optional<PartStock> findByPartIdForUpdate(UUID partId);
}
