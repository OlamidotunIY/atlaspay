package com.atlaspay.auth.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record VerificationCompletedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        VerificationPayload payload
) implements DomainEvent<VerificationPayload> {}
