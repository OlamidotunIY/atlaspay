package com.atlaspay.identity.domain.event;

import com.atlaspay.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantEmailVerificationResent(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<MerchantEmailVerificationResent.Payload> {
    public record Payload(
        String email,
        String verificationCode
    ) {}
}
