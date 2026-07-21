package core.access.events;

import core.access.valueobjects.ApiKeyId;
import core.identity.valueobject.CompanyId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyIssued(UUID eventId, Instant occurredOn, ApiKeyId apiKeyId,
                           CompanyId ownerCompanyId) implements DomainEvent {
}

