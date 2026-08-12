package com.atlaspay.shared.exception;

public abstract class DomainException extends AtlasPayException {

    protected DomainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
