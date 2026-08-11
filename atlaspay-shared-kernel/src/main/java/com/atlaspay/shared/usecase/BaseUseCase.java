package com.atlaspay.shared.usecase;

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
}
