package core.identity.events;

import core.identity.valueobject.CompanyId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CompanySuspended(UUID eventId, Instant occurredOn, CompanyId companyId,
                               String reason) implements DomainEvent {
}
