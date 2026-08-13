package com.atlaspay.accounts.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record VirtualAccountCreatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Void payload
) implements DomainEvent<Void> {}
