package com.atlaspay.identity.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlaspay.identity.application.command.RevokeApiKeyCommand;

import com.atlaspay.shared.usecase.BaseUseCase;

import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.domain.repository.ApiKeyRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;

public class RevokeApiKeyUseCase extends BaseUseCase<RevokeApiKeyCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(RevokeApiKeyUseCase.class);


    private final ApiKeyRepository apiKeyRepository;
    private final DomainEventPublisher eventPublisher;

    public RevokeApiKeyUseCase(ApiKeyRepository apiKeyRepository, DomainEventPublisher eventPublisher) {
        this.apiKeyRepository = apiKeyRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void execute(RevokeApiKeyCommand command) {
        log.info("Executing RevokeApiKeyUseCase");

        ApiKey apiKey = apiKeyRepository.findById(command.keyId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.API_KEY_NOT_FOUND, "API Key not found"));

        if (!apiKey.getMerchantId().equals(command.authenticatedMerchantId())) {
            throw new NotFoundException(IdentityErrorCode.API_KEY_NOT_FOUND, "API Key not found");
        }

        apiKey.revoke();

        apiKeyRepository.save(apiKey);
        publishEvents(apiKey, eventPublisher);
    
        return null;
    }
}
