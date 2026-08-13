package com.atlaspay.shared.exception;

public class RateLimitExceededException extends AtlasPayException {
    
    private final long retryAfterSeconds;
    private final long limit;

    public RateLimitExceededException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.retryAfterSeconds = 0;
        this.limit = 0;
    }

    public RateLimitExceededException(ErrorCode errorCode, String message, long retryAfterSeconds, long limit) {
        super(errorCode, message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.limit = limit;
    }
    
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
    
    public long getLimit() {
        return limit;
    }
}
