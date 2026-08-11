package com.atlaspay.shared.exception;

/**
 * Thrown when an operation conflicts with the current state of a resource.
 * Maps to HTTP 409 Conflict.
 *
 * <p>Usage: duplicate email, duplicate idempotency key, optimistic locking conflict.</p>
 */
public class ConflictException extends DomainException {

    public ConflictException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ConflictException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
