package com.atlaspay.identity.domain.event;

import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantEmailVerificationResent(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    String correlationId
) implements DomainEvent {}
