package org.example.orderservice.infrastructure.jpa.repository;

import org.example.orderservice.domain.auth.model.AuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface JpaAuthRefreshTokenRepository extends JpaRepository<AuthRefreshToken, UUID> {

    Optional<AuthRefreshToken> findByTokenHash(String tokenHash);

    long deleteByExpiresAtBefore(Instant threshold);

    @Modifying
    @Query("""
            update AuthRefreshToken token
               set token.revokedAt = :revokedAt
             where token.familyId = :familyId
               and token.revokedAt is null
            """)
    void revokeFamily(@Param("familyId") UUID familyId, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("""
            update AuthRefreshToken token
               set token.revokedAt = :revokedAt
             where token.userId = :userId
               and token.revokedAt is null
            """)
    void revokeAllByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);
}
