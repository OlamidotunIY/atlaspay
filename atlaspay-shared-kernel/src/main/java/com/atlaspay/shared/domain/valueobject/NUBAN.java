package com.atlaspay.shared.domain.valueobject;

import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;

public record NUBAN(String value) {
    public NUBAN {
        if (value == null || value.isBlank()) {
            throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "NUBAN cannot be null or blank");
        }
        if (!value.matches("^\\d{10}$")) {
            throw new ValidationException(SharedErrorCode.INVALID_NUBAN_FORMAT, "NUBAN must be exactly 10 digits");
        }
    }
}
