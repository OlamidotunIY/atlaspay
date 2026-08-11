package com.atlaspay.shared.exception;

/**
 * Abstract base for application / infrastructure-level exceptions.
 * These represent failures outside the domain model itself — authorization,
 * external service calls, infrastructure faults, etc.
 *
 * <p>Concrete subclasses map to specific HTTP status codes via {@code @ControllerAdvice}.</p>
 */
public abstract class ApplicationException extends AtlasPayException {

    protected ApplicationException(String errorCode, String message) {
        super(errorCode, message);
    }

    protected ApplicationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
