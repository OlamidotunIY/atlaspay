package core.transfers.events;

import core.ledger.valueobjects.Money;
import core.shared.DomainEvent;
import core.transfers.valueobjects.TransferId;

import java.time.Instant;
import java.util.UUID;

public record TransferInitiated(UUID eventId, Instant occurredOn, TransferId transferId,
                                Money amount) implements DomainEvent {
}

