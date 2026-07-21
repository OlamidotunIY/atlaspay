package core.access.events;

import core.access.valueobjects.ApiKeyId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyRevoked(UUID eventId, Instant occurredOn, ApiKeyId apiKeyId) implements DomainEvent {}
