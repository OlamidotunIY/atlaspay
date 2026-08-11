package com.atlaspay.shared.exception;

/**
 * Thrown when a requested resource does not exist.
 * Maps to HTTP 404 Not Found.
 *
 * <p>Usage: user not found, transaction not found, account not found.</p>
 */
public class NotFoundException extends DomainException {

    public NotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }

    public NotFoundException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
