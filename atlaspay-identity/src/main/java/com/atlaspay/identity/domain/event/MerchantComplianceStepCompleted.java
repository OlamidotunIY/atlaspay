package com.atlaspay.identity.domain.event;

import com.atlaspay.identity.domain.model.ComplianceStep;
import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantComplianceStepCompleted(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    String correlationId,
    Payload payload
) implements DomainEvent<MerchantComplianceStepCompleted.Payload> {
    public record Payload(ComplianceStep step) {}
}
