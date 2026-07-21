package core.access.events;

import core.access.valueobjects.WebhookSubscriptionId;
import core.identity.valueobject.CompanyId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record WebhookSubscriptionCreated(UUID eventId, Instant occurredOn, WebhookSubscriptionId subscriptionId, CompanyId ownerCompanyId) implements DomainEvent {}
