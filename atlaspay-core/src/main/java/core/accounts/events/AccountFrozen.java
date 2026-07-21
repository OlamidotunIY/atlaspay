package core.accounts.events;

import core.accounts.valueobjects.AccountId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AccountFrozen(UUID eventId, Instant occurredOn, AccountId accountId, String reason) implements DomainEvent {}
