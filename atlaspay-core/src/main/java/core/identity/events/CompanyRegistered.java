package core.identity.events;

import core.identity.valueobject.CompanyId;
import core.identity.valueobject.CompanyName;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CompanyRegistered(UUID eventId, Instant occurredOn, CompanyId companyId,
                                CompanyName name) implements DomainEvent {
}
