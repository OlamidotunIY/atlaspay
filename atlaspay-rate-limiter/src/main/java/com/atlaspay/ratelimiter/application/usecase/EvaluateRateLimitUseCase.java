package com.atlaspay.ratelimiter.application.usecase;

import com.atlaspay.ratelimiter.domain.RateLimitRule;
import com.atlaspay.ratelimiter.domain.RateLimitAlgorithm;
import com.atlaspay.ratelimiter.domain.exception.RateLimiterErrorCode;
import com.atlaspay.ratelimiter.infrastructure.adapter.RedisSlidingWindowRateLimiter;
import com.atlaspay.ratelimiter.infrastructure.adapter.RedisTokenBucketRateLimiter;
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

    public void execute(String key, RateLimitRule rule) {
        boolean allowed;
        
        if (rule.algorithm() == RateLimitAlgorithm.TOKEN_BUCKET) {
            allowed = tokenBucketLimiter.isAllowed(key, rule);
        } else if (rule.algorithm() == RateLimitAlgorithm.SLIDING_WINDOW) {
            allowed = slidingWindowLimiter.isAllowed(key, rule);
        } else {
            throw new IllegalArgumentException("Unknown rate limit algorithm: " + rule.algorithm());
        }

        if (!allowed) {
            throw new RateLimitExceededException(RateLimiterErrorCode.RATE_LIMIT_EXCEEDED, RateLimiterErrorCode.RATE_LIMIT_EXCEEDED.getDefaultMessage());
        }
    }
}
