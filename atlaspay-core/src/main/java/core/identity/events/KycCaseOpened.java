package core.identity.events;

import core.identity.valueobject.CustomerId;
import core.identity.valueobject.KycCaseId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record KycCaseOpened(UUID eventId, Instant occurredOn, KycCaseId kycCaseId, CustomerId customerId) implements DomainEvent {}
