package com.atlaspay.notifications.infrastructure.messaging.dto;

public record VirtualAccountActivatedNotificationEvent(
        String eventId,
        String eventType,
        String aggregateId,
        Payload payload
) {
    public record Payload(Long integration, String nuban) {}
}
