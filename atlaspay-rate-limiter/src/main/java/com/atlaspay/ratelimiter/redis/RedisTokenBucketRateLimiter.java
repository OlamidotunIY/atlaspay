package com.atlaspay.ratelimiter.redis;

import com.atlaspay.ratelimiter.core.RateLimitResult;
import com.atlaspay.ratelimiter.core.RateLimiterPort;
import com.atlaspay.ratelimiter.core.RateLimitRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.dao.DataAccessException;

import java.util.Collections;
import java.util.List;

@Component
public class RedisTokenBucketRateLimiter implements RateLimiterPort {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenBucketRateLimiter.class);
    
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> script;

    public RedisTokenBucketRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        this.script = new DefaultRedisScript<>();
        this.script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/token_bucket.lua")));
        this.script.setResultType(List.class);
    }

    @Override
    public RateLimitResult evaluate(String key, RateLimitRule rule) {
        List<String> keys = Collections.singletonList("rate_limit:tb:" + key);
        long now = System.currentTimeMillis() / 1000;
        
        try {
            List<Long> result = redisTemplate.execute(
                script,
                keys,
                String.valueOf(rule.capacity()),
                String.valueOf(rule.refillRatePerSecond()),
                String.valueOf(now)
            );
            
            if (result != null && result.size() == 3) {
                boolean allowed = result.get(0) == 1L;
                long remaining = result.get(1);
                long retryAfter = result.get(2);
                
                if (allowed) {
                    return RateLimitResult.allowed(remaining, rule.capacity());
                } else {
                    return RateLimitResult.rejected(remaining, rule.capacity(), retryAfter);
                }
            }
        } catch (DataAccessException e) {
            log.warn("Redis is down or timed out. Failing open for rate limit key: {}", key, e);
            // Fail-open strategy
            return RateLimitResult.allowed(rule.capacity(), rule.capacity());
        }
        
        // Fallback fail-open if script returns malformed result
        return RateLimitResult.allowed(rule.capacity(), rule.capacity());
    }
}
