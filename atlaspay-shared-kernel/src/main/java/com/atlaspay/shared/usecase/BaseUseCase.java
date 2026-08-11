package com.atlaspay.shared.usecase;

import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.event.EnvelopedDomainEvent;

/**
 * Base abstract class for all application use cases.
 * I - Input Port (Command or Query)
 * O - Output Type
 */
public abstract class BaseUseCase<I, O> {
    
    /**
     * Executes the use case.
     * @param input The input command or query
     * @return The result of the use case
     */
    public abstract O execute(I input);

    /**
     * Helper method to publish all domain events from an aggregate root.
     */
    protected void publishEvents(AggregateRoot<?> aggregate, DomainEventPublisher publisher) {
        aggregate.pullDomainEvents().forEach(event -> 
            publisher.publish(EnvelopedDomainEvent.wrap(event))
        );
    }
}
