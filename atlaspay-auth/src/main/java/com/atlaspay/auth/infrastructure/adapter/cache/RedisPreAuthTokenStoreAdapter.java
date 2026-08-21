package com.atlaspay.auth.infrastructure.adapter.cache;

import com.atlaspay.auth.application.port.out.PreAuthTokenStorePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class RedisPreAuthTokenStoreAdapter implements PreAuthTokenStorePort {

    private final StringRedisTemplate redisTemplate;
    private static final String PRE_AUTH_PREFIX = "token:preauth:";

    public RedisPreAuthTokenStoreAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void store(String token, Long authAccountId) {
        // Pre-auth tokens live for 5 minutes
        redisTemplate.opsForValue().set(PRE_AUTH_PREFIX + token, String.valueOf(authAccountId), 300, TimeUnit.SECONDS);
    }

    @Override
    public Optional<Long> consume(String token) {
        String value = redisTemplate.opsForValue().get(PRE_AUTH_PREFIX + token);
        if (value != null) {
            redisTemplate.delete(PRE_AUTH_PREFIX + token); // consume means read and delete
            return Optional.of(Long.parseLong(value));
        }
        return Optional.empty();
    }
}
