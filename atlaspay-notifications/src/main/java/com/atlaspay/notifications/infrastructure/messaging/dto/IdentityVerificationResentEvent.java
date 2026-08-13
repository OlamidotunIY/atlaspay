package com.atlaspay.notifications.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IdentityVerificationResentEvent(
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
