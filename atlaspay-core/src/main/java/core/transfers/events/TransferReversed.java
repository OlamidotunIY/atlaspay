package core.transfers.events;

import core.ledger.valueobjects.JournalEntryId;
import core.shared.DomainEvent;
import core.transfers.valueobjects.TransferId;

import java.time.Instant;
import java.util.UUID;

public record TransferReversed(UUID eventId, Instant occurredOn, TransferId transferId, JournalEntryId compensatingEntryId) implements DomainEvent {}
