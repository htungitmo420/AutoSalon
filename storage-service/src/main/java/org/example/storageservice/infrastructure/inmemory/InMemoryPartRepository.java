package org.example.storageservice.infrastructure.inmemory;

import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.part.models.Part;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryPartRepository implements PartRepository {

    private final ConcurrentHashMap<UUID, Part> storage = new ConcurrentHashMap<>();

    @Override
    public Part save(Part part) {
        if (part == null) throw new IllegalArgumentException("Part must not be null");
        if (part.getId() == null) {
            part.setId(UUID.randomUUID());
        }
        storage.put(part.getId(), part);
        return part;
    }

    @Override
    public Optional<Part> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Part> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Part> findByType(PartType type) {
        if (type == null) return List.of();
        return storage.values().stream()
                .filter(p -> p.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(UUID id) {
        return storage.remove(id) != null;
    }
}