package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.domain.repository.ApiKeyRepository;
import com.atlaspay.shared.event.DomainEvent;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
import com.atlaspay.shared.exception.NotFoundException;

public class RevokeApiKeyUseCase {

    private final ApiKeyRepository apiKeyRepository;
    private final DomainEventPublisher eventPublisher;

    public RevokeApiKeyUseCase(ApiKeyRepository apiKeyRepository, DomainEventPublisher eventPublisher) {
        this.apiKeyRepository = apiKeyRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(RevokeApiKeyCommand command) {
        ApiKey apiKey = apiKeyRepository.findById(command.keyId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.API_KEY_NOT_FOUND, "API Key not found"));

        if (!apiKey.getMerchantId().equals(command.authenticatedMerchantId())) {
            throw new NotFoundException(IdentityErrorCode.API_KEY_NOT_FOUND, "API Key not found");
        }

        apiKey.revoke();

        apiKeyRepository.save(apiKey);
        apiKey.pullDomainEvents().forEach(this::publishEvent);
    }

    private <T> void publishEvent(DomainEvent<T> event) {
        eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
    }
}
