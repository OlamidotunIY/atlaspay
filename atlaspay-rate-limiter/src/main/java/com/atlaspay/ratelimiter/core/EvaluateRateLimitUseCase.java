package com.atlaspay.ratelimiter.core;

import com.atlaspay.ratelimiter.exception.RateLimiterErrorCode;
import com.atlaspay.ratelimiter.redis.RedisSlidingWindowRateLimiter;
import com.atlaspay.ratelimiter.redis.RedisTokenBucketRateLimiter;
import com.atlaspay.shared.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

@Service
public class EvaluateRateLimitUseCase {

    private final RedisTokenBucketRateLimiter tokenBucketLimiter;
    private final RedisSlidingWindowRateLimiter slidingWindowLimiter;

    public EvaluateRateLimitUseCase(
            RedisTokenBucketRateLimiter tokenBucketLimiter,
            RedisSlidingWindowRateLimiter slidingWindowLimiter) {
        this.tokenBucketLimiter = tokenBucketLimiter;
        this.slidingWindowLimiter = slidingWindowLimiter;
    }

    public RateLimitResult execute(String key, RateLimitRule rule) {
        RateLimitResult result;
        
        if (rule.algorithm() == RateLimitAlgorithm.TOKEN_BUCKET) {
            result = tokenBucketLimiter.evaluate(key, rule);
        } else if (rule.algorithm() == RateLimitAlgorithm.SLIDING_WINDOW) {
            result = slidingWindowLimiter.evaluate(key, rule);
        } else {
            throw new IllegalArgumentException("Unknown rate limit algorithm: " + rule.algorithm());
        }

        if (!result.isAllowed()) {
            throw new RateLimitExceededException(
                RateLimiterErrorCode.RATE_LIMIT_EXCEEDED, 
                RateLimiterErrorCode.RATE_LIMIT_EXCEEDED.getDefaultMessage(),
                result.retryAfterSeconds(),
                result.limit()
            );
        }
        
        return result;
    }
}
