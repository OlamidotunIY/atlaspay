package com.atlaspay.identity.domain.event;

import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantProfileUpdated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<MerchantProfileUpdated.Payload> {
    public record Payload(
        String firstName,
        String lastName,
        String businessName,
        String phone
    ) {}
}
