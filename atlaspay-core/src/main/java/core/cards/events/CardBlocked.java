package core.cards.events;

import core.cards.valueobjects.CardId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CardBlocked(UUID eventId, Instant occurredOn, CardId cardId, String reason) implements DomainEvent {}
