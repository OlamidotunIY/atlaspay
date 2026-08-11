package com.atlaspay.shared.exception;

/**
 * Thrown when a call to an external service (payment provider, KYC vendor, etc.) fails.
 * Maps to HTTP 502 Bad Gateway.
 */
public class ExternalServiceException extends ApplicationException {

    public ExternalServiceException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ExternalServiceException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
