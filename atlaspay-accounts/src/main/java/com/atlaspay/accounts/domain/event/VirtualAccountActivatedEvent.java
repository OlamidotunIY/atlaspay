package com.atlaspay.accounts.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record VirtualAccountActivatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        String payload // NUBAN value
) implements DomainEvent<String> {}
