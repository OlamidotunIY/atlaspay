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

import java.util.Arrays;
import java.util.List;

@Component
public class RedisSlidingWindowRateLimiter implements RateLimiterPort {

    private static final Logger log = LoggerFactory.getLogger(RedisSlidingWindowRateLimiter.class);

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> script;

    public RedisSlidingWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        this.script = new DefaultRedisScript<>();
        this.script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/sliding_window_counter.lua")));
        this.script.setResultType(List.class);
    }

    @Override
    public RateLimitResult evaluate(String key, RateLimitRule rule) {
        long nowMs = System.currentTimeMillis();
        long windowSizeMs = rule.windowSizeSeconds() * 1000L;
        
        // Calculate the current window timestamp
        long currentWindowStartMs = (nowMs / windowSizeMs) * windowSizeMs;
        long previousWindowStartMs = currentWindowStartMs - windowSizeMs;
        
        String currentKey = "rate_limit:sw:" + key + ":" + currentWindowStartMs;
        String previousKey = "rate_limit:sw:" + key + ":" + previousWindowStartMs;
        
        List<String> keys = Arrays.asList(currentKey, previousKey);
        
        try {
            List<Long> result = redisTemplate.execute(
                script,
                keys,
                String.valueOf(rule.windowSizeSeconds()),
                String.valueOf(rule.maxRequests()),
                String.valueOf(nowMs),
                String.valueOf(currentWindowStartMs)
            );
            
            if (result != null && result.size() == 3) {
                boolean allowed = result.get(0) == 1L;
                long remaining = result.get(1);
                long retryAfter = result.get(2);
                
                if (allowed) {
                    return RateLimitResult.allowed(remaining, rule.maxRequests());
                } else {
                    return RateLimitResult.rejected(remaining, rule.maxRequests(), retryAfter);
                }
            }
        } catch (DataAccessException e) {
            log.warn("Redis is down or timed out. Failing open for rate limit key: {}", key, e);
            // Fail-open strategy
            return RateLimitResult.allowed(rule.maxRequests(), rule.maxRequests());
        }
        
        // Fallback fail-open if script returns malformed result
        return RateLimitResult.allowed(rule.maxRequests(), rule.maxRequests());
    }
}
