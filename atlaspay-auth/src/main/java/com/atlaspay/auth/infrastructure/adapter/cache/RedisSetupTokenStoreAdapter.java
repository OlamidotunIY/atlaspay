package com.atlaspay.auth.infrastructure.adapter.cache;

import com.atlaspay.auth.application.port.out.SetupTokenStorePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class RedisSetupTokenStoreAdapter implements SetupTokenStorePort {

    private final StringRedisTemplate redisTemplate;
    private static final String SETUP_PREFIX = "token:setup:";

    public RedisSetupTokenStoreAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void store(String token, Long authAccountId) {
        // Setup tokens live for 10 minutes
        redisTemplate.opsForValue().set(SETUP_PREFIX + token, String.valueOf(authAccountId), 600, TimeUnit.SECONDS);
    }

    @Override
    public Optional<Long> consume(String token) {
        String value = redisTemplate.opsForValue().get(SETUP_PREFIX + token);
        if (value != null) {
            redisTemplate.delete(SETUP_PREFIX + token);
            return Optional.of(Long.parseLong(value));
        }
        return Optional.empty();
    }
}