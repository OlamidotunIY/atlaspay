package com.atlaspay.notifications.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IdentityRegistrationEvent(
    String eventId,
    String aggregateId,
    String occurredAt,
    Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
        String email,
        String verificationCode
    ) {}
}
