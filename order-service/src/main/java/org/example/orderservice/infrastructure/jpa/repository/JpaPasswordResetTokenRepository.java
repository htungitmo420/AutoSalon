package org.example.orderservice.infrastructure.jpa.repository;

import org.example.orderservice.domain.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    long deleteByExpiresAtBefore(Instant threshold);

    @Modifying
    @Query("""
            update PasswordResetToken token
               set token.consumedAt = :consumedAt
             where token.userId = :userId
               and token.consumedAt is null
            """)
    void invalidateAllByUserId(@Param("userId") UUID userId, @Param("consumedAt") Instant consumedAt);
}
