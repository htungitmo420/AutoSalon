package org.example.orderservice.infrastructure.inmemory;

import org.example.orderservice.application.repository.AuthRefreshTokenRepository;
import org.example.orderservice.domain.auth.model.AuthRefreshToken;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAuthRefreshTokenRepository implements AuthRefreshTokenRepository {

    private final Map<String, AuthRefreshToken> store = new ConcurrentHashMap<>();

    @Override
    public AuthRefreshToken save(AuthRefreshToken token) {
        if (token.getId() == null) {
            token.setId(UUID.randomUUID());
        }
        store.put(token.getTokenHash(), token);
        return token;
    }

    @Override
    public Optional<AuthRefreshToken> findByTokenHash(String tokenHash) {
        return Optional.ofNullable(store.get(tokenHash));
    }

    @Override
    public void revokeFamily(UUID familyId, Instant revokedAt) {
        store.values().stream()
                .filter(token -> token.getFamilyId().equals(familyId))
                .filter(token -> token.getRevokedAt() == null)
                .forEach(token -> token.setRevokedAt(revokedAt));
    }

    @Override
    public void revokeAllByUserId(UUID userId, Instant revokedAt) {
        store.values().stream()
                .filter(token -> token.getUserId().equals(userId))
                .filter(token -> token.getRevokedAt() == null)
                .forEach(token -> token.setRevokedAt(revokedAt));
    }
}
