package core.access.events;

import core.access.valueobjects.WebhookDeliveryId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record WebhookDeliveryRetryScheduled(UUID eventId, Instant occurredOn, WebhookDeliveryId deliveryId, int attemptCount) implements DomainEvent {}
