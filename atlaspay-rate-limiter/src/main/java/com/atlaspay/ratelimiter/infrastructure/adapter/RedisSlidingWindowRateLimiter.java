package com.atlaspay.ratelimiter.infrastructure.adapter;

import com.atlaspay.ratelimiter.application.port.out.RateLimiterPort;
import com.atlaspay.ratelimiter.domain.RateLimitRule;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RedisSlidingWindowRateLimiter implements RateLimiterPort {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> script;

    public RedisSlidingWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        String lua = """
            local key = KEYS[1]
            local windowSize = tonumber(ARGV[1])
            local maxRequests = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            
            local windowStart = now - (windowSize * 1000)
            
            -- Remove all elements outside the window
            redis.call('ZREMRANGEBYSCORE', key, '-inf', windowStart)
            
            -- Count elements in the window
            local count = redis.call('ZCARD', key)
            
            if count < maxRequests then
                -- Generate unique score/member by combining timestamp with a random or incrementing value
                -- Since this script runs atomically, just using the 'now' timestamp with a microsecond precision or simply 'now'
                -- To handle identical millisecond requests, we append count
                local member = tostring(now) .. '-' .. tostring(count)
                redis.call('ZADD', key, now, member)
                redis.call('EXPIRE', key, windowSize)
                return 1
            else
                return 0
            end
            """;
            
        this.script = new DefaultRedisScript<>(lua, Long.class);
    }

    @Override
    public boolean isAllowed(String key, RateLimitRule rule) {
        List<String> keys = Collections.singletonList("rate_limit:sw:" + key);
        long now = System.currentTimeMillis();
        
        Long result = redisTemplate.execute(
            script,
            keys,
            String.valueOf(rule.windowSizeSeconds()),
            String.valueOf(rule.maxRequests()),
            String.valueOf(now)
        );
        
        return result != null && result == 1L;
    }
}
