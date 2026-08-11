package com.atlaspay.shared.domain.id;

import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;

import java.util.UUID;

public record ApiKeyId(String value) {
    public ApiKeyId {
        if (value == null || value.isBlank()) {
            throw new ValidationException(SharedErrorCode.INVALID_ID, "ApiKey ID cannot be null or blank");
        }
    }
    
    public static ApiKeyId generate() {
        return new ApiKeyId(UUID.randomUUID().toString());
    }
}
