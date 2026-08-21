package com.atlaspay.auth.infrastructure.adapter.cache;

import com.atlaspay.auth.application.port.out.TokenCachePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class RedisTokenCacheAdapter implements TokenCachePort {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String SESSION_PREFIX = "token:session:";

    public RedisTokenCacheAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void cacheSession(String token, ZonedDateTime expiresAt) {
        long ttlSeconds = Duration.between(ZonedDateTime.now(), expiresAt).getSeconds();
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(SESSION_PREFIX + token, "valid", ttlSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void blacklistToken(String token, ZonedDateTime expiresAt) {
        long ttlSeconds = Duration.between(ZonedDateTime.now(), expiresAt).getSeconds();
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
            redisTemplate.delete(SESSION_PREFIX + token);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
