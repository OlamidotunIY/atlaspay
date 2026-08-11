package com.atlaspay.shared.exception;

public class BusinessRuleException extends DomainException {

    public BusinessRuleException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessRuleException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
