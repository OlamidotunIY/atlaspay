package com.atlaspay.auth.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record SessionRevokedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        SessionPayload payload
) implements DomainEvent<SessionPayload> {}
