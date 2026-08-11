package com.atlaspay.shared.domain.id;

import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;

public record UserId(String value) {
    public UserId {
        if (value == null || value.isBlank()) {
            throw new ValidationException(SharedErrorCode.INVALID_ID, "UserId cannot be empty");
        }
    }
}
