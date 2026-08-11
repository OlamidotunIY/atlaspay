package com.atlaspay.shared.exception;

/**
 * Base sealed class for all AtlasPay domain exceptions.
 * Unchecked exception because we rely on Spring's @ControllerAdvice for mapping to HTTP responses.
 */
public abstract class AtlasPayException extends RuntimeException {
    
    public AtlasPayException(String message) {
        super(message);
    }
    
    public AtlasPayException(String message, Throwable cause) {
        super(message, cause);
    }
}
