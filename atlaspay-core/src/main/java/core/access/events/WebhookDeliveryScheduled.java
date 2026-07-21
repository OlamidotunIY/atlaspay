package core.access.events;

import core.access.valueobjects.WebhookDeliveryId;
import core.access.valueobjects.WebhookSubscriptionId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record WebhookDeliveryScheduled(UUID eventId, Instant occurredOn, WebhookDeliveryId deliveryId, WebhookSubscriptionId subscriptionId) implements DomainEvent {}
