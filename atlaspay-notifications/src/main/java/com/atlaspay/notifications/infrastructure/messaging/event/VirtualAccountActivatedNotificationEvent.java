package com.atlaspay.notifications.infrastructure.messaging.event;

public record VirtualAccountActivatedNotificationEvent(
        String eventId,
        String eventType,
        String aggregateId,
        Payload payload
) {
    public record Payload(Long integration, String nuban) {}
}
