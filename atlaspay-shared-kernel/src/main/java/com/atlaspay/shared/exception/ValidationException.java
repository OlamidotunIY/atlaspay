package com.atlaspay.shared.exception;

/**
 * Thrown when input data fails validation rules.
 * Maps to HTTP 400 Bad Request.
 *
 * <p>Usage: invalid field format, missing required field, out-of-range value.</p>
 */
public class ValidationException extends DomainException {

    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ValidationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
