package com.atlaspay.identity.domain.event;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record ApiKeyRevoked(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<ApiKeyRevoked.Payload> {
    public record Payload(
        String merchantId,
        KeyType keyType,
        ApiEnvironment environment
    ) {}
}
