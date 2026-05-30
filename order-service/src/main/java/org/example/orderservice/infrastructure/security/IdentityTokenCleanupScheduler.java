package org.example.orderservice.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.infrastructure.jpa.repository.JpaAuthRateLimitRepository;
import org.example.orderservice.infrastructure.jpa.repository.JpaAuthRefreshTokenRepository;
import org.example.orderservice.infrastructure.jpa.repository.JpaPasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityTokenCleanupScheduler {

    private final JpaAuthRefreshTokenRepository refreshTokenRepository;
    private final JpaPasswordResetTokenRepository passwordResetTokenRepository;
    private final JpaAuthRateLimitRepository rateLimitRepository;

    @Value("${security.auth.cleanup.retention-seconds:604800}")
    private long retentionSeconds;

    @Scheduled(fixedDelayString = "${security.auth.cleanup.fixed-delay-ms:3600000}")
    @Transactional
    public void deleteExpiredIdentityData() {
        Instant threshold = Instant.now().minusSeconds(retentionSeconds);
        long refreshTokens = refreshTokenRepository.deleteByExpiresAtBefore(threshold);
        long passwordResetTokens = passwordResetTokenRepository.deleteByExpiresAtBefore(threshold);
        long rateLimits = rateLimitRepository.deleteByUpdatedAtBefore(threshold);

        if (refreshTokens + passwordResetTokens + rateLimits > 0) {
            log.info(
                    "Deleted expired identity data: refreshTokens={}, passwordResetTokens={}, rateLimits={}",
                    refreshTokens,
                    passwordResetTokens,
                    rateLimits);
        }
    }
}
