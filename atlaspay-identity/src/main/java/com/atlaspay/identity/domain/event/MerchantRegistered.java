package com.atlaspay.identity.domain.event;

import com.atlaspay.identity.domain.model.BusinessType;
import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantRegistered(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    String correlationId,
    Payload payload
) implements DomainEvent<MerchantRegistered.Payload> {
    public record Payload(
        String businessName,
        String email,
        String country,
        BusinessType businessType
    ) {}
}
