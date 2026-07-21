package core.cards.events;

import core.cards.valueobjects.DisputeId;
import core.cards.valueobjects.DisputeStatus;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record DisputeResolved(UUID eventId, Instant occurredOn, DisputeId disputeId, DisputeStatus outcome) implements DomainEvent {}
