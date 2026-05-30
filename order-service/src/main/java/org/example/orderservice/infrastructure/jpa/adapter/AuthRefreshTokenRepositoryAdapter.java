package org.example.orderservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.repository.AuthRefreshTokenRepository;
import org.example.orderservice.domain.auth.model.AuthRefreshToken;
import org.example.orderservice.infrastructure.jpa.repository.JpaAuthRefreshTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuthRefreshTokenRepositoryAdapter implements AuthRefreshTokenRepository {

    private final JpaAuthRefreshTokenRepository delegate;

    @Override
    public AuthRefreshToken save(AuthRefreshToken token) {
        return delegate.save(token);
    }

    @Override
    public Optional<AuthRefreshToken> findByTokenHash(String tokenHash) {
        return delegate.findByTokenHash(tokenHash);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeFamily(UUID familyId, Instant revokedAt) {
        delegate.revokeFamily(familyId, revokedAt);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllByUserId(UUID userId, Instant revokedAt) {
        delegate.revokeAllByUserId(userId, revokedAt);
    }
}
