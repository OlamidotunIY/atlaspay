package com.atlaspay.shared.exception;

/**
 * Error codes for exceptions thrown from the shared-kernel itself.
 */
public enum SharedErrorCode implements ErrorCode {
    MONEY_NEGATIVE_AMOUNT,
    INVALID_ID,
    INVALID_EMAIL_FORMAT,
    INVALID_PHONE_FORMAT
}
