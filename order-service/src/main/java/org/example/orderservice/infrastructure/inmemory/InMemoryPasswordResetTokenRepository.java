package org.example.orderservice.infrastructure.inmemory;

import org.example.orderservice.application.repository.PasswordResetTokenRepository;
import org.example.orderservice.domain.auth.model.PasswordResetToken;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPasswordResetTokenRepository implements PasswordResetTokenRepository {

    private final Map<String, PasswordResetToken> store = new ConcurrentHashMap<>();

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        if (token.getId() == null) {
            token.setId(UUID.randomUUID());
        }
        store.put(token.getTokenHash(), token);
        return token;
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return Optional.ofNullable(store.get(tokenHash));
    }

    @Override
    public void invalidateAllByUserId(UUID userId, Instant consumedAt) {
        store.values().stream()
                .filter(token -> token.getUserId().equals(userId))
                .filter(token -> token.getConsumedAt() == null)
                .forEach(token -> token.setConsumedAt(consumedAt));
    }
}
