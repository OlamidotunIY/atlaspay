package com.atlaspay.shared.domain;

import com.atlaspay.shared.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all aggregate roots in the AtlasPay domain.
 *
 * <h3>Domain event pattern</h3>
 * <ol>
 *   <li>Domain methods call {@link #registerEvent(DomainEvent)} to record what happened.</li>
 *   <li>The application layer calls {@link #pullDomainEvents()} after a successful
 *       repository save, then hands each event to {@code DomainEventPublisher}.</li>
 * </ol>
 *
 * <p>This keeps the domain completely ignorant of the publishing infrastructure
 * and makes aggregates trivially unit-testable — just assert on
 * {@code aggregate.pullDomainEvents()} after calling a domain method.</p>
 *
 * <pre>{@code
 * // Domain layer
 * public class User extends AggregateRoot<UserId> {
 *     public void register(String email) {
 *         // ... state change ...
 *         registerEvent(new UserRegistered(
 *             UUID.randomUUID().toString(),
 *             getId().value(),
 *             ZonedDateTime.now(),
 *             CorrelationId.getOrCreate(),
 *             email
 *         ));
 *     }
 * }
 *
 * // Application layer
 * User user = new User(...);
 * user.register(command.email());
 * userRepository.save(user);
 * user.pullDomainEvents().forEach(publisher::publish);
 * }</pre>
 *
 * @param <ID> the type of the aggregate's identity (e.g. {@code UserId}, {@code AccountId})
 */
public abstract class AggregateRoot<ID> {

    private final List<DomainEvent<?>> domainEvents = new ArrayList<>();

    /**
     * Returns the unique identity of this aggregate.
     */
    public abstract ID getId();

    /**
     * Registers a domain event to be published after the aggregate is persisted.
     * Call this inside domain methods whenever state changes occur.
     *
     * @param event the event that just occurred
     */
    protected void registerEvent(DomainEvent<?> event) {
        domainEvents.add(event);
    }

    /**
     * Drains and returns all pending domain events.
     * The internal list is cleared after this call — events are delivered exactly once.
     *
     * <p>Called by the application layer after a successful {@code repository.save()}.</p>
     *
     * @return an immutable snapshot of the accumulated events
     */
    public List<DomainEvent<?>> pullDomainEvents() {
        List<DomainEvent<?>> snapshot = List.copyOf(domainEvents);
        domainEvents.clear();
        return snapshot;
    }

    /**
     * Returns a read-only view of pending events without clearing them.
     * Useful for testing or inspection without side effects.
     */
    public List<DomainEvent<?>> peekDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
