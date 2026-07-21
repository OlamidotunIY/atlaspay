package core.cards.events;

import core.accounts.valueobjects.AccountId;
import core.cards.valueobjects.CardId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CardIssued(UUID eventId, Instant occurredOn, CardId cardId, AccountId linkedAccountId) implements DomainEvent {}

