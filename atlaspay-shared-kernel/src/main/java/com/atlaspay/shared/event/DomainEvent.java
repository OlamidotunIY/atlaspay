package com.atlaspay.shared.event;

import java.time.ZonedDateTime;

/**
 * Base interface for all domain events.
 * A domain event is a record of something that has already happened in the domain.
 */
public interface DomainEvent {
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
}
