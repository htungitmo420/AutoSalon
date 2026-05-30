package org.example.orderservice.application.repository;

import org.example.orderservice.domain.auth.model.AuthRefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthRefreshTokenRepository {

    AuthRefreshToken save(AuthRefreshToken token);

    Optional<AuthRefreshToken> findByTokenHash(String tokenHash);

    void revokeFamily(UUID familyId, Instant revokedAt);

    void revokeAllByUserId(UUID userId, Instant revokedAt);
}
