package com.atlaspay.ledger.domain.event;

import com.atlaspay.shared.event.DomainEvent;

import com.atlaspay.ledger.domain.model.SourceSystem;

public record LedgerTransactionPostedEvent(
    String eventId,
    String aggregateId,
    java.time.ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<LedgerTransactionPostedEvent.Payload> {
    public record Payload(
        String transactionReference,
        SourceSystem sourceSystem
    ) {}
}
