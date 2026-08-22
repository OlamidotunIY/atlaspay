package com.atlaspay.auth.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record SessionCreatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        SessionPayload payload
) implements DomainEvent<SessionPayload> {}
