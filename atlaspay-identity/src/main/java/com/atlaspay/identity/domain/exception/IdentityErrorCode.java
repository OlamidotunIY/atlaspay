package com.atlaspay.identity.domain.exception;

import com.atlaspay.shared.exception.ErrorCode;

/**
 * All error codes specific to the Identity bounded context.
 */
public enum IdentityErrorCode implements ErrorCode {
    USER_NOT_FOUND,
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_EXISTS,
    USER_LOCKED,
    MERCHANT_SUSPENDED
}
