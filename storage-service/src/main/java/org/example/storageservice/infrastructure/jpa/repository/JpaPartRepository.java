package org.example.storageservice.infrastructure.jpa.repository;

import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.part.models.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaPartRepository extends JpaRepository<Part, UUID> {

    Optional<Part> findByIdAndRemovedFalse(UUID id);

    List<Part> findAllByRemovedFalse();

    List<Part> findByTypeAndRemovedFalse(PartType type);
}