package com.atlaspay.ratelimiter.domain.exception;

import com.atlaspay.shared.exception.ErrorCode;

public enum RateLimiterErrorCode implements ErrorCode {
    RATE_LIMIT_EXCEEDED("RTL_001", "Too many requests. Please try again later.");

    private final String code;
    private final String defaultMessage;

    RateLimiterErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
