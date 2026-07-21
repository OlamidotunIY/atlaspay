package core.transfers.events;

import core.ledger.valueobjects.JournalEntryId;
import core.shared.DomainEvent;
import core.transfers.valueobjects.TransferId;

import java.time.Instant;
import java.util.UUID;

public record TransferPosted(UUID eventId, Instant occurredOn, TransferId transferId, JournalEntryId entryId) implements DomainEvent {}
