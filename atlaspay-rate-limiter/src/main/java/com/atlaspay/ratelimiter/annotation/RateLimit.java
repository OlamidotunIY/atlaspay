package com.atlaspay.ratelimiter.annotation;

import com.atlaspay.ratelimiter.core.RateLimitKeyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * The unique ID of the rule configured in the system/Redis.
     * e.g., "strict_auth_sw", "merchant_creation_tb"
     */
    String ruleId();
    
    /**
     * How to resolve the key for this limit.
     */
    RateLimitKeyType keyType() default RateLimitKeyType.MERCHANT_ID;
}
