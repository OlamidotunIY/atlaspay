package core.identity.events;

import core.identity.valueobject.CompanyId;
import core.identity.valueobject.CustomerId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CustomerOnboarded(UUID eventId, Instant occurredOn, CustomerId customerId, CompanyId companyId) implements DomainEvent {}

