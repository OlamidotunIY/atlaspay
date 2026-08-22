package com.atlaspay.auth.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record PasswordSetupInitiatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<PasswordSetupInitiatedEvent.Payload> {
    public record Payload(String identifier, String setupToken) {}
}