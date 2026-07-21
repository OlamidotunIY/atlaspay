package core.cards.events;

import core.cards.valueobjects.DisputeId;
import core.cards.valueobjects.ReasonCode;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record DisputeFiled(UUID eventId, Instant occurredOn, DisputeId disputeId, ReasonCode reasonCode) implements DomainEvent {}
