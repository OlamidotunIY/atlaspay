package com.atlaspay.ledger.domain.event;

import com.atlaspay.shared.event.DomainEvent;

public record FundsReservationFailedEvent(
    String eventId,
    String aggregateId,
    java.time.ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<FundsReservationFailedEvent.Payload> {
    public record Payload(
        String reference,
        java.math.BigDecimal amount,
        String currency,
        String reason
    ) {}
}
