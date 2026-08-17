package com.atlaspay.ledger.domain.event;

import com.atlaspay.shared.event.DomainEvent;

public record FundsReservedEvent(
    String eventId,
    String aggregateId,
    java.time.ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<FundsReservedEvent.Payload> {
    public record Payload(
        String reference,
        java.math.BigDecimal amount,
        String currency
    ) {}
}
