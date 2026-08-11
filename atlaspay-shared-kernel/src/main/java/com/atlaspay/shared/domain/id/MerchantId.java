package com.atlaspay.shared.domain.id;

import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;

public record MerchantId(String value) {
    public MerchantId {
        if (value == null || value.isBlank()) {
            throw new ValidationException(SharedErrorCode.INVALID_ID, "MerchantId cannot be empty");
        }
    }
}
