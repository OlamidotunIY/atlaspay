package com.atlaspay.ratelimiter.application.port.out;

import com.atlaspay.ratelimiter.domain.RateLimitRule;

public interface RateLimiterPort {
    /**
     * Checks if the given key is allowed to proceed under the specified rule.
     * 
     * @param key The unique key (e.g., IP address, Merchant ID)
     * @param rule The rule defining the limits
     * @return true if allowed, false if limit exceeded
     */
    boolean isAllowed(String key, RateLimitRule rule);
}
