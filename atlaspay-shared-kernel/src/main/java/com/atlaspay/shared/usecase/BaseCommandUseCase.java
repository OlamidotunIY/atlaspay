package com.atlaspay.shared.usecase;

/**
 * Base abstract class for command use cases that do not return a result.
 * I - Input Command
 */
public abstract class BaseCommandUseCase<I> {

    /**
     * Executes the command use case.
     * @param input The input command
     */
    public abstract void execute(I input);

    /**
     * Helper method to publish all domain events from an aggregate root.
     */
    protected void publishEvents(com.atlaspay.shared.domain.AggregateRoot<?> aggregate, com.atlaspay.shared.event.DomainEventPublisher publisher) {
        aggregate.pullDomainEvents().forEach(event -> 
            publisher.publish(com.atlaspay.shared.event.EnvelopedDomainEvent.wrap(event))
        );
    }
}
