package com.atlaspay.shared.event;

import java.time.ZonedDateTime;
import java.util.Map;

public record WebhookDeliveryRequestedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) implements DomainEvent<WebhookDeliveryRequestedEvent.Payload> {

    public record Payload(
            String endpointUrl,
            String payloadJson,
            Map<String, String> headers
    ) {}
}
