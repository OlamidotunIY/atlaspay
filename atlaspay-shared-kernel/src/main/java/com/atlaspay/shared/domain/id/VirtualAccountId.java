package com.atlaspay.shared.domain.id;

import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;

import java.util.UUID;

public record VirtualAccountId(String value) {
    public VirtualAccountId {
        if (value == null || value.isBlank()) {
            throw new ValidationException(SharedErrorCode.INVALID_ID, "SubAccount ID cannot be null or blank");
        }
    }

    public static VirtualAccountId generate() {
        return new VirtualAccountId(UUID.randomUUID().toString());
    }
}
