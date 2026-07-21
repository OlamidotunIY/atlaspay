package core.cards.events;

import core.cards.valueobjects.CardId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CardActivated(UUID eventId, Instant occurredOn, CardId cardId) implements DomainEvent {}
