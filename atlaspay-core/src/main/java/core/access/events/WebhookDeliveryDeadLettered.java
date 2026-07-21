package core.access.events;

import core.access.valueobjects.WebhookDeliveryId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record WebhookDeliveryDeadLettered(UUID eventId, Instant occurredOn, WebhookDeliveryId deliveryId) implements DomainEvent {}
