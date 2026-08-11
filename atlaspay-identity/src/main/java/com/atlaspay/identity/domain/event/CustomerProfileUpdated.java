package com.atlaspay.identity.domain.event;

import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record CustomerProfileUpdated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<CustomerProfileUpdated.Payload> {
    public record Payload(
        String merchantId,
        String firstName,
        String lastName,
        String phone
    ) {}
}
