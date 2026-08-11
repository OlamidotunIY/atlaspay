package com.atlaspay.shared.domain.id;

import java.util.UUID;

public record SubAccountId(String value) {
    public SubAccountId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SubAccount ID cannot be null or blank");
        }
    }
    
    public static SubAccountId generate() {
        return new SubAccountId(UUID.randomUUID().toString());
    }
}
