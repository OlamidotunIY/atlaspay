package core.limits.events;

import core.accounts.valueobjects.AccountId;
import core.limits.valueobjects.LimitPolicyId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record LimitBreached(UUID eventId, Instant occurredOn, AccountId accountId, LimitPolicyId policyId) implements DomainEvent {}
