package com.atlaspay.shared.exception;

import lombok.Getter;

/**
 * Base sealed class for all AtlasPay domain and application exceptions.
 *
 * <p>Every concrete exception must carry a machine-readable {@code errorCode}
 * (e.g. {@code "IDENTITY_USER_NOT_FOUND"}) so that a single {@code @ControllerAdvice}
 * can map the hierarchy to HTTP responses without switching on class types.</p>
 *
 * <pre>
 * AtlasPayException
 * ├── DomainException       (pure business-rule violations)
 * │   ├── ValidationException       → 400
 * │   ├── NotFoundException         → 404
 * │   ├── ConflictException         → 409
 * │   └── BusinessRuleException     → 422
 * └── ApplicationException  (application / infrastructure concerns)
 *     ├── AuthorizationException    → 403
 *     └── ExternalServiceException  → 502
 * </pre>
 */
@Getter
public abstract class AtlasPayException extends RuntimeException {

    /**
     * -- GETTER --
     *  Machine-readable error code used by
     *  to map to an HTTP response.
     *  Convention:
     *  e.g.
     * ,
     */
    private final String errorCode;

    protected AtlasPayException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected AtlasPayException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
