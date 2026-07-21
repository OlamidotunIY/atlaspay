package core.accounts.events;

import core.accounts.valueobjects.AccountId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AccountClosed(UUID eventId, Instant occurredOn, AccountId accountId) implements DomainEvent {}
