package com.atlaspay.shared.exception;

public class RateLimitExceededException extends AtlasPayException {
    
    public RateLimitExceededException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
