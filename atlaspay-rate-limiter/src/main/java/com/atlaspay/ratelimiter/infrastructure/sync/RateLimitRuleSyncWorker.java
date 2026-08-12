package com.atlaspay.ratelimiter.infrastructure.sync;

import com.atlaspay.ratelimiter.domain.RateLimitAlgorithm;
import com.atlaspay.ratelimiter.domain.RateLimitRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RateLimitRuleSyncWorker {

    private static final Logger log = LoggerFactory.getLogger(RateLimitRuleSyncWorker.class);
    private final StringRedisTemplate redisTemplate;
    private static final String RULES_KEY_PREFIX = "rate_limit:rules:";

    public RateLimitRuleSyncWorker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void syncRulesToRedis() {
        log.info("Starting Rate Limit Rules sync to Redis...");
        
        // In a real application, these rules would be fetched from a database
        // For now, we simulate fetching dynamic rules
        List<RateLimitRule> rulesToSync = fetchDynamicRulesFromDatabase();
        
        for (RateLimitRule rule : rulesToSync) {
            String key = RULES_KEY_PREFIX + rule.ruleId();
            
            Map<String, String> ruleMap = new HashMap<>();
            ruleMap.put("algorithm", rule.algorithm().name());
            ruleMap.put("capacity", String.valueOf(rule.capacity()));
            ruleMap.put("refillRatePerSecond", String.valueOf(rule.refillRatePerSecond()));
            ruleMap.put("windowSizeSeconds", String.valueOf(rule.windowSizeSeconds()));
            ruleMap.put("maxRequests", String.valueOf(rule.maxRequests()));
            
            redisTemplate.opsForHash().putAll(key, ruleMap);
            log.debug("Synced rule {} to Redis", rule.ruleId());
        }
        
        log.info("Completed Rate Limit Rules sync.");
    }
    
    private List<RateLimitRule> fetchDynamicRulesFromDatabase() {
        return List.of(
            // Global IP limit (1000 tokens, 10 tokens/sec refill)
            new RateLimitRule("global_ip_tb", RateLimitAlgorithm.TOKEN_BUCKET, 1000, 10, 0, 0),
            
            // Stricter limit for login/registration (5 requests per minute)
            new RateLimitRule("strict_auth_sw", RateLimitAlgorithm.SLIDING_WINDOW, 0, 0, 60, 5)
        );
    }
}
