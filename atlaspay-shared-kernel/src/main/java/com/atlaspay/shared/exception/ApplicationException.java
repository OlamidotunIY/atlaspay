package com.atlaspay.shared.exception;

public abstract class ApplicationException extends AtlasPayException {

    protected ApplicationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
