package com.atlaspay.identity.domain.event;

import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantComplianceApproved(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt
) implements DomainEvent<Void> {
    @Override
    public Void payload() {
        return null;
    }
}
