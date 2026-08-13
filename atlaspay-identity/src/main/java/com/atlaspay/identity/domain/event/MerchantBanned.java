package com.atlaspay.identity.domain.event;

import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantBanned(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<MerchantBanned.Payload> {
    
    public record Payload(String reason) {}
}
