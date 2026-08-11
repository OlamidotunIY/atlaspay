package com.atlaspay.shared.exception;

/**
 * Thrown when an operation is syntactically valid but violates a core business rule.
 * Maps to HTTP 422 Unprocessable Entity.
 *
 * <p>Usage: insufficient funds, transfer to same account, account suspended,
 * exceeding a business-defined limit.</p>
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessRuleException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
