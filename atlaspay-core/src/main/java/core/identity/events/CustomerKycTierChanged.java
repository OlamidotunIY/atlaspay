package core.identity.events;

import core.identity.valueobject.CustomerId;
import core.identity.valueobject.KycTier;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CustomerKycTierChanged(UUID eventId, Instant occurredOn, CustomerId customerId, KycTier oldTier, KycTier newTier) implements DomainEvent {}
