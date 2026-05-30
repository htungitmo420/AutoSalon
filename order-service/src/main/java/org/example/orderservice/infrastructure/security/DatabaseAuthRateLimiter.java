package org.example.orderservice.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.exception.RateLimitExceededException;
import org.example.orderservice.application.port.out.AuthRateLimiter;
import org.example.orderservice.application.port.out.OpaqueTokenProvider;
import org.example.orderservice.infrastructure.jpa.entity.AuthRateLimit;
import org.example.orderservice.infrastructure.jpa.repository.JpaAuthRateLimitRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DatabaseAuthRateLimiter implements AuthRateLimiter {

    private final JpaAuthRateLimitRepository repository;
    private final OpaqueTokenProvider tokenProvider;

    @Value("${security.auth.rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.auth.rate-limit.window-seconds:900}")
    private long windowSeconds;

    @Value("${security.auth.rate-limit.block-seconds:900}")
    private long blockSeconds;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureAllowed(String action, String clientKey) {
        repository.findByActionAndKeyHash(action, keyHash(action, clientKey))
                .filter(limit -> isBlocked(limit, Instant.now()))
                .ifPresent(limit -> {
                    throw new RateLimitExceededException("Too many attempts. Try again later");
                });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String action, String clientKey) {
        Instant now = Instant.now();
        String keyHash = keyHash(action, clientKey);
        repository.ensureExists(UUID.randomUUID(), action, keyHash, now);
        AuthRateLimit limit = repository.findByActionAndKeyHash(action, keyHash)
                .orElseThrow(() -> new IllegalStateException("Rate limit bucket was not created"));

        if (!limit.getWindowStartedAt().plusSeconds(windowSeconds).isAfter(now)) {
            limit.setAttempts(0);
            limit.setWindowStartedAt(now);
            limit.setBlockedUntil(null);
        }

        int attempts = limit.getAttempts() + 1;
        limit.setAttempts(attempts);
        limit.setUpdatedAt(now);
        if (attempts >= maxAttempts) {
            limit.setBlockedUntil(now.plusSeconds(blockSeconds));
        }
        repository.save(limit);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clear(String action, String clientKey) {
        repository.deleteByActionAndKeyHash(action, keyHash(action, clientKey));
    }

    private boolean isBlocked(AuthRateLimit limit, Instant now) {
        return limit.getBlockedUntil() != null && limit.getBlockedUntil().isAfter(now);
    }

    private String keyHash(String action, String clientKey) {
        return tokenProvider.hash(action + ":" + clientKey);
    }
}
