package com.atlaspay.identity.domain.event;

import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantComplianceRejected(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    String correlationId,
    Payload payload
) implements DomainEvent<MerchantComplianceRejected.Payload> {
    public record Payload(String reason) {}
}
