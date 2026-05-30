package org.example.orderservice.application.repository;

import org.example.orderservice.domain.auth.model.PasswordResetToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void invalidateAllByUserId(UUID userId, Instant consumedAt);
}
