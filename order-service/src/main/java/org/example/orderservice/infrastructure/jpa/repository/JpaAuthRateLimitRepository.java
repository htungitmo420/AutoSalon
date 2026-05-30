package org.example.orderservice.infrastructure.jpa.repository;

import jakarta.persistence.LockModeType;
import org.example.orderservice.infrastructure.jpa.entity.AuthRateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface JpaAuthRateLimitRepository extends JpaRepository<AuthRateLimit, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthRateLimit> findByActionAndKeyHash(String action, String keyHash);

    @Modifying
    @Query(value = """
            insert into auto_salon.auth_rate_limits
                (id, action, key_hash, attempts, window_started_at, updated_at)
            values
                (:id, :action, :keyHash, 0, :now, :now)
            on conflict (action, key_hash) do nothing
            """, nativeQuery = true)
    void ensureExists(
            @Param("id") UUID id,
            @Param("action") String action,
            @Param("keyHash") String keyHash,
            @Param("now") Instant now);

    void deleteByActionAndKeyHash(String action, String keyHash);

    long deleteByUpdatedAtBefore(Instant threshold);
}
