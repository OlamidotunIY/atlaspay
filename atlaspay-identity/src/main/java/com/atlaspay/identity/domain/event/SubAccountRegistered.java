package com.atlaspay.identity.domain.event;

import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record SubAccountRegistered(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<SubAccountRegistered.Payload> {
    public record Payload(
        String merchantId,
        String bankCode,
        String accountNumber,
        String accountName
    ) {}
}
