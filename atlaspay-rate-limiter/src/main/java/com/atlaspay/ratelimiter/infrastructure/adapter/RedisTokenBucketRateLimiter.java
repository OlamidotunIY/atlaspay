package com.atlaspay.ratelimiter.infrastructure.adapter;

import com.atlaspay.ratelimiter.application.port.out.RateLimiterPort;
import com.atlaspay.ratelimiter.domain.RateLimitRule;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RedisTokenBucketRateLimiter implements RateLimiterPort {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> script;

    public RedisTokenBucketRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        String lua = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refillRate = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            
            local bucket = redis.call('HMGET', key, 'tokens', 'lastRefill')
            local tokens = tonumber(bucket[1])
            local lastRefill = tonumber(bucket[2])
            
            if tokens == nil then
                tokens = capacity
                lastRefill = now
            end
            
            local timePassed = math.max(0, now - lastRefill)
            local tokensToAdd = math.floor(timePassed * refillRate)
            
            if tokensToAdd > 0 then
                tokens = math.min(capacity, tokens + tokensToAdd)
                lastRefill = now
            end
            
            if tokens >= 1 then
                tokens = tokens - 1
                redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', lastRefill)
                -- Expire after time needed to refill fully to avoid memory leaks
                local ttl = math.ceil(capacity / refillRate)
                redis.call('EXPIRE', key, ttl)
                return 1
            else
                redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', lastRefill)
                return 0
            end
            """;
            
        this.script = new DefaultRedisScript<>(lua, Long.class);
    }

    @Override
    public boolean isAllowed(String key, RateLimitRule rule) {
        List<String> keys = Collections.singletonList("rate_limit:tb:" + key);
        long now = System.currentTimeMillis() / 1000;
        
        Long result = redisTemplate.execute(
            script,
            keys,
            String.valueOf(rule.capacity()),
            String.valueOf(rule.refillRatePerSecond()),
            String.valueOf(now)
        );
        
        return result != null && result == 1L;
    }
}
