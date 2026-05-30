package org.example.orderservice.infrastructure.inmemory;

import org.example.orderservice.application.repository.AuthUserRepository;
import org.example.orderservice.domain.auth.model.AuthUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAuthUserRepository implements AuthUserRepository {

    private final Map<UUID, AuthUser> store = new ConcurrentHashMap<>();

    @Override
    public AuthUser save(AuthUser entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Auth user must not be null");
        }
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<AuthUser> findById(UUID id) {
        return Optional.ofNullable(store.get(id))
                .filter(user -> !user.isRemoved());
    }

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        String normalizedEmail = email == null ? "" : email.toLowerCase(Locale.ROOT);
        return store.values().stream()
                .filter(user -> !user.isRemoved())
                .filter(user -> user.getEmail().equalsIgnoreCase(normalizedEmail))
                .findFirst();
    }

    @Override
    public List<AuthUser> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean deleteById(UUID id) {
        AuthUser user = store.get(id);
        if (user == null || user.isRemoved()) {
            return false;
        }
        user.setRemoved(true);
        return true;
    }
}
