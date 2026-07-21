package core.transfers.events;

import core.shared.DomainEvent;
import core.transfers.valueobjects.TransferId;

import java.time.Instant;
import java.util.UUID;

public record TransferFailed(UUID eventId, Instant occurredOn, TransferId transferId, String reason) implements DomainEvent {}
