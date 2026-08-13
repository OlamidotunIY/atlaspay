package com.atlaspay.accounts.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record VirtualAccountActivatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<VirtualAccountActivatedEvent.Payload> {
    
    public record Payload(Long integration, String nuban) {}
}
