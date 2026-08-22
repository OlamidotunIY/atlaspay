package com.atlaspay.auth.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record VerificationCreatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        VerificationPayload payload
) implements DomainEvent<VerificationPayload> {}
