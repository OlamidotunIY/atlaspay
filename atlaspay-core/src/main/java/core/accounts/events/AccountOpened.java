package core.accounts.events;

import core.accounts.valueobjects.AccountId;
import core.identity.valueobject.CustomerId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AccountOpened(UUID eventId, Instant occurredOn, AccountId accountId, CustomerId ownerId) implements DomainEvent {}

