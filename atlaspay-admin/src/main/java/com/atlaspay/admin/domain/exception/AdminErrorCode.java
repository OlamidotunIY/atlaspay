package com.atlaspay.admin.domain.exception;

import com.atlaspay.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AdminErrorCode implements ErrorCode {
    ADMIN_NOT_FOUND("ADM_001", "Admin not found", HttpStatus.NOT_FOUND),
    ADMIN_EMAIL_ALREADY_EXISTS("ADM_002", "Admin personal email already exists", HttpStatus.CONFLICT),
    COMPANY_EMAIL_ALREADY_GENERATED("ADM_003", "Admin already has a company email", HttpStatus.CONFLICT);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    AdminErrorCode(String code, String defaultMessage, HttpStatus status) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return status;
    }
}

