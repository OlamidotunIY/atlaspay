package com.atlaspay.shared.exception;

/**
 * Abstract base for all pure domain / business-rule exceptions.
 * These represent things the domain explicitly rejected — not infrastructure failures.
 *
 * <p>Concrete subclasses map to specific HTTP 4xx status codes via {@code @ControllerAdvice}.</p>
 */
public abstract class DomainException extends AtlasPayException {

    protected DomainException(String errorCode, String message) {
        super(errorCode, message);
    }

    protected DomainException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
