package com.atlaspay.shared.domain.id;

import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;
import java.util.UUID;

public record OutboxMessageId(String value) {
    public OutboxMessageId {
        if (value == null || value.isBlank()) {
            throw new ValidationException(SharedErrorCode.INVALID_ID, "OutboxMessageId cannot be empty");
        }
    }
    
    public static OutboxMessageId generate() {
        return new OutboxMessageId(UUID.randomUUID().toString());
    }
}
