package com.atlaspay.ratelimiter.infrastructure.adapter;

import com.atlaspay.ratelimiter.application.port.out.RateLimitRuleProvider;
import com.atlaspay.ratelimiter.domain.RateLimitAlgorithm;
import com.atlaspay.ratelimiter.domain.RateLimitRule;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisRateLimitRuleProvider implements RateLimitRuleProvider {

    private final StringRedisTemplate redisTemplate;
    private static final String RULES_KEY_PREFIX = "rate_limit:rules:";

    public RedisRateLimitRuleProvider(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitRule getRule(String ruleId) {
        String key = RULES_KEY_PREFIX + ruleId;
        Map<Object, Object> ruleData = redisTemplate.opsForHash().entries(key);
        
        if (ruleData.isEmpty()) {
            // Default rules fallback if not found in Redis
            if (ruleId.endsWith("_sw")) {
                return RateLimitRule.defaultSlidingWindow();
            }
            return RateLimitRule.defaultTokenBucket();
        }
        
        return new RateLimitRule(
            ruleId,
            RateLimitAlgorithm.valueOf((String) ruleData.get("algorithm")),
            Integer.parseInt((String) ruleData.get("capacity")),
            Integer.parseInt((String) ruleData.get("refillRatePerSecond")),
            Integer.parseInt((String) ruleData.get("windowSizeSeconds")),
            Integer.parseInt((String) ruleData.get("maxRequests"))
        );
    }
}
