package core.identity.events;

import core.identity.valueobject.CustomerId;
import core.identity.valueobject.KycStatus;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CustomerKycDecided(UUID eventId, Instant occurredOn, CustomerId customerId, KycStatus status) implements DomainEvent {}
