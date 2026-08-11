package com.atlaspay.shared.event;

import java.time.ZonedDateTime;

/**
 * Base interface for all domain events.
 *
 * <p>A domain event is a record of something that has already happened in the domain.
 * Events are <em>raised inside the aggregate</em> (via {@code registerEvent}) and
 * <em>published by the application layer</em> after a successful transaction commit.</p>
 *
 * <p>Implementors should be immutable records. Example:</p>
 * <pre>{@code
 * public record UserRegistered(
 *     String eventId,
 *     String aggregateId,
 *     ZonedDateTime occurredAt,
 *     String correlationId,
 *     String email
 * ) implements DomainEvent {}
 * }</pre>
 */
public interface DomainEvent<T> {

    /**
     * Unique identifier for this specific event instance.
     */
    String eventId();

    /**
     * The ID of the aggregate that produced this event.
     */
    String aggregateId();

    /**
     * The time the event occurred.
     */
    ZonedDateTime occurredAt();

    /**
     * The domain-specific payload of the event.
     * Null if the event has no payload.
     */
    T payload();
}
