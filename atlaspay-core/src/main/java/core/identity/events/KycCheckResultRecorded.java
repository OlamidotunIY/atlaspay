package core.identity.events;

import core.identity.valueobject.KycCaseId;
import core.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record KycCheckResultRecorded(UUID eventId, Instant occurredOn, KycCaseId kycCaseId, String checkName, boolean passed) implements DomainEvent {}
