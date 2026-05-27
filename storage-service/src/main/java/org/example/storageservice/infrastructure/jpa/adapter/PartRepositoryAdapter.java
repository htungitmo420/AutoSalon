package org.example.storageservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.part.models.Part;
import org.example.storageservice.infrastructure.jpa.repository.JpaPartRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PartRepositoryAdapter implements PartRepository {

    private final JpaPartRepository delegate;

    @Override
    public Part save(Part entity) {
        return delegate.save(entity);
    }

    @Override
    public Optional<Part> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public List<Part> findAll() {
        return delegate.findAllByRemovedFalse();
    }

    @Override
    public List<Part> findByType(PartType type) {
        return type == null ? List.of() : delegate.findByTypeAndRemovedFalse(type);
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<Part> existing = delegate.findByIdAndRemovedFalse(id);
        if (existing.isEmpty()) {
            return false;
        }
        Part entity = existing.get();
        entity.setRemoved(true);
        delegate.save(entity);
        return true;
    }
}