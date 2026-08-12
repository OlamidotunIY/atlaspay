package com.atlaspay.ratelimiter.domain;

public record RateLimitRule(
    String ruleId,
    RateLimitAlgorithm algorithm,
    int capacity,             // Max tokens for Token Bucket
    int refillRatePerSecond,  // Refill rate for Token Bucket
    int windowSizeSeconds,    // Window size for Sliding Window
    int maxRequests           // Threshold for Sliding Window
) {
    public static RateLimitRule defaultTokenBucket() {
        return new RateLimitRule("default_tb", RateLimitAlgorithm.TOKEN_BUCKET, 100, 10, 0, 0);
    }
    
    public static RateLimitRule defaultSlidingWindow() {
        return new RateLimitRule("default_sw", RateLimitAlgorithm.SLIDING_WINDOW, 0, 0, 60, 5);
    }
}
