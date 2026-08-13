package com.atlaspay.ratelimiter.core;

public record RateLimitResult(
    boolean isAllowed,
    long remainingRequests,
    long limit,
    long retryAfterSeconds
) {
    public static RateLimitResult allowed(long remainingRequests, long limit) {
        return new RateLimitResult(true, remainingRequests, limit, 0);
    }
    
    public static RateLimitResult rejected(long remainingRequests, long limit, long retryAfterSeconds) {
        return new RateLimitResult(false, remainingRequests, limit, retryAfterSeconds);
    }
}
