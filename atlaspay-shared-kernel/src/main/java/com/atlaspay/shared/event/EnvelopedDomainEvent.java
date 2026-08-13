package com.atlaspay.shared.event;

import com.atlaspay.shared.tracing.CorrelationId;

import java.time.ZonedDateTime;

/**
 * A wrapper for Domain Events that attaches infrastructure-level concerns
 * (like correlation IDs and dispatch times) right before the event is
 * published to the event bus or saved to an outbox table.
 *
 * This keeps the core DomainEvent interface pure and free of HTTP/Tracing
 * concepts.
 *
 * @param <T> The payload type of the wrapped DomainEvent
 */
public record EnvelopedDomainEvent<T>(
    String eventType,
    DomainEvent<T> event,
    String correlationId,
    ZonedDateTime dispatchedAt
) {
    /**
     * Wraps a pure DomainEvent with the current thread's Correlation ID.
     * This should be called by the Application Service / Event Dispatcher.
     *
     * @param event The pure domain event emitted by the aggregate
     * @return The enveloped event ready for publication
     */
    public static <T> EnvelopedDomainEvent<T> wrap(DomainEvent<T> event) {
        return new EnvelopedDomainEvent<>(
            event.getClass().getSimpleName(),
            event,
            CorrelationId.getOrCreate(),
            ZonedDateTime.now()
        );
    }
}
