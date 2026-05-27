package org.example.storageservice.infrastructure.jpa.repository;

import org.example.storageservice.domain.car.model.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCarModelRepository extends JpaRepository<CarModel, UUID> {

	Optional<CarModel> findByIdAndRemovedFalse(UUID id);

	List<CarModel> findAllByRemovedFalse();
}