package core.ledger.events;

import core.ledger.valueobjects.JournalEntryId;
import core.ledger.valueobjects.TransactionReference;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record JournalEntryPosted(UUID eventId, Instant occurredOn, JournalEntryId entryId, TransactionReference reference) implements DomainEvent {
}
