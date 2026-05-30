package org.example.orderservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.repository.PasswordResetTokenRepository;
import org.example.orderservice.domain.auth.model.PasswordResetToken;
import org.example.orderservice.infrastructure.jpa.repository.JpaPasswordResetTokenRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final JpaPasswordResetTokenRepository delegate;

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        return delegate.save(token);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return delegate.findByTokenHash(tokenHash);
    }

    @Override
    public void invalidateAllByUserId(UUID userId, Instant consumedAt) {
        delegate.invalidateAllByUserId(userId, consumedAt);
    }
}
