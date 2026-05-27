package org.example.storageservice.infrastructure.jpa.repository;

import org.example.storageservice.domain.car.model.Car;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCarRepository extends JpaRepository<Car, UUID>, JpaSpecificationExecutor<Car> {

	@EntityGraph(attributePaths = "model")
	Optional<Car> findByIdAndRemovedFalse(UUID id);

	@EntityGraph(attributePaths = "model")
	List<Car> findAllByRemovedFalse();

	@EntityGraph(attributePaths = "model")
	List<Car> findAllByRemovedFalseAndTestDriveFalse();
}