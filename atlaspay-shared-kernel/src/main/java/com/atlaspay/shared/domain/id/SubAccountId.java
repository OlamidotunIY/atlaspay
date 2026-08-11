package com.atlaspay.shared.domain.id;

import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;

import java.util.UUID;

public record SubAccountId(String value) {
    public SubAccountId {
        if (value == null || value.isBlank()) {
            throw new ValidationException(SharedErrorCode.INVALID_ID, "SubAccount ID cannot be null or blank");
        }
    }
    
    public static SubAccountId generate() {
        return new SubAccountId(UUID.randomUUID().toString());
    }
}
