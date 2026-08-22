package com.atlaspay.auth.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record AdminCredentialsCreatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<AdminCredentialsCreatedEvent.Payload> {
    public record Payload(String personalEmail, String temporaryPassword) {}
}