package com.atlaspay.ratelimiter.application.port.out;

import com.atlaspay.ratelimiter.domain.RateLimitRule;

public interface RateLimitRuleProvider {
    /**
     * Retrieves the rate limit rule for a specific rule ID.
     * @param ruleId The ID of the rule
     * @return The rule, or a default rule if not found
     */
    RateLimitRule getRule(String ruleId);
}
