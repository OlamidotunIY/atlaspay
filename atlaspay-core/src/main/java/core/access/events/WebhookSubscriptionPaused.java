package core.access.events;

import core.access.valueobjects.WebhookSubscriptionId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record WebhookSubscriptionPaused(UUID eventId, Instant occurredOn, WebhookSubscriptionId subscriptionId) implements DomainEvent {}
