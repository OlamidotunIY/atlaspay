package com.atlaspay.shared.exception;

/**
 * Thrown when the caller lacks permission to perform the operation.
 * Maps to HTTP 403 Forbidden.
 */
public class AuthorizationException extends ApplicationException {

    public AuthorizationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AuthorizationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
