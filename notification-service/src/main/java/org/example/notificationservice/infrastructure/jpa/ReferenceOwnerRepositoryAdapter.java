package org.example.notificationservice.infrastructure.jpa;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.application.repository.ReferenceOwnerRepository;
import org.example.notificationservice.domain.projection.ReferenceOwner;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReferenceOwnerRepositoryAdapter implements ReferenceOwnerRepository {
    private final JpaReferenceOwnerRepository repository;

    @Override
    public void upsert(ReferenceOwner owner) {
        repository.upsert(owner.getReferenceId(), owner.getCustomerId(), owner.getReferenceType(), owner.getUpdatedAt());
    }

    @Override
    public Optional<ReferenceOwner> findById(UUID referenceId) {
        return repository.findById(referenceId);
    }
}
