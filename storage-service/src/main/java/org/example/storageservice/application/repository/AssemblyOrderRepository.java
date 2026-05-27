package org.example.storageservice.application.repository;

import org.example.storageservice.domain.assembly.model.AssemblyOrder;

import java.util.List;
import java.util.UUID;

public interface AssemblyOrderRepository extends Repository<AssemblyOrder> {

    List<AssemblyOrder> findAllBySourceOrderId(UUID sourceOrderId);

    List<AssemblyOrder> findAllByCarId(UUID carId);
}
