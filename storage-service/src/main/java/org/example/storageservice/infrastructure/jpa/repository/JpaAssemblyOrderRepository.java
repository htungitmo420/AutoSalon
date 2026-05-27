package org.example.storageservice.infrastructure.jpa.repository;

import org.example.storageservice.domain.assembly.model.AssemblyOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaAssemblyOrderRepository extends JpaRepository<AssemblyOrder, UUID> {

    Optional<AssemblyOrder> findByIdAndRemovedFalse(UUID id);

    List<AssemblyOrder> findAllByRemovedFalse();

    List<AssemblyOrder> findAllBySourceOrderIdAndRemovedFalse(UUID sourceOrderId);

    List<AssemblyOrder> findAllByCarIdAndRemovedFalse(UUID carId);
}
