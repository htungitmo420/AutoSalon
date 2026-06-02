package org.example.cartservice.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.example.cartservice.application.port.out.CartExpiryIndex;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisCartExpiryIndex implements CartExpiryIndex {

    private static final String KEY_PREFIX = "cart:expiry:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void track(UUID cartId, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            remove(cartId);
            return;
        }
        redisTemplate.opsForValue().set(key(cartId), "active", ttl);
    }

    @Override
    public void remove(UUID cartId) {
        redisTemplate.delete(key(cartId));
    }

    private String key(UUID cartId) {
        return KEY_PREFIX + cartId;
    }
}
