package org.example.notificationservice.application.repository;

import org.example.notificationservice.domain.projection.ReferenceOwner;

import java.util.Optional;
import java.util.UUID;

public interface ReferenceOwnerRepository {
    void upsert(ReferenceOwner owner);
    Optional<ReferenceOwner> findById(UUID referenceId);
}
