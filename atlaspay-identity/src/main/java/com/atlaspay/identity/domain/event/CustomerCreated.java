package com.atlaspay.identity.domain.event;

import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record CustomerCreated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<CustomerCreated.Payload> {
    public record Payload(
        String merchantId,
        String email,
        String firstName,
        String lastName
    ) {}
}
