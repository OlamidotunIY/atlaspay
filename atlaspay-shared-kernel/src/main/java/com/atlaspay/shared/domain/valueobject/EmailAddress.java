package com.atlaspay.shared.domain.valueobject;

import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;

import java.util.regex.Pattern;

public record EmailAddress(String value) {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new ValidationException(SharedErrorCode.INVALID_EMAIL_FORMAT, "Email address cannot be empty");
        }
        if (value.length() > 254) {
            throw new ValidationException(SharedErrorCode.INVALID_EMAIL_FORMAT, "Email address cannot exceed 254 characters");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new ValidationException(SharedErrorCode.INVALID_EMAIL_FORMAT, "Invalid email address format");
        }
    }
}
