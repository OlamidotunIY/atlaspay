package com.atlaspay.shared.idempotency;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a unique key provided by the client (or generated internally)
 * to ensure a mutating operation is idempotent.
 */
public record IdempotencyKey(String value) {
    public IdempotencyKey {
        Objects.requireNonNull(value, "Idempotency key value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Idempotency key value cannot be blank");
        }
    }

    public static IdempotencyKey generate() {
        return new IdempotencyKey(UUID.randomUUID().toString());
    }
}
