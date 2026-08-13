package com.atlaspay.ratelimiter.core;

import com.atlaspay.ratelimiter.core.RateLimitRule;

public interface RateLimiterPort {
    /**
     * Checks if the given key is allowed to proceed under the specified rule.
     * 
     * @param key The unique key (e.g., IP address, Merchant ID)
     * @param rule The rule defining the limits
     * @return RateLimitResult containing allowed status, remaining tokens, and retry-after
     */
    RateLimitResult evaluate(String key, RateLimitRule rule);
}
