package core.limits.events;

import core.identity.valueobject.KycTier;
import core.limits.valueobjects.LimitPolicyId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record LimitPolicyCreated(UUID eventId, Instant occurredOn, LimitPolicyId policyId, KycTier applicableTier) implements DomainEvent {}
