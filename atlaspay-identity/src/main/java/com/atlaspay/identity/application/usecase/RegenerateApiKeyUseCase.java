package com.atlaspay.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlaspay.identity.application.command.RegenerateApiKeyCommand;

import com.atlaspay.shared.usecase.BaseUseCase;

import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.ApiKeyRepository;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.NotFoundException;

import java.util.UUID;

@Service
public class RegenerateApiKeyUseCase extends BaseUseCase<RegenerateApiKeyCommand, String> {
    private static final Logger log = LoggerFactory.getLogger(RegenerateApiKeyUseCase.class);


    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final DomainEventPublisher eventPublisher;

    public RegenerateApiKeyUseCase(
            MerchantRepository merchantRepository,
            ApiKeyRepository apiKeyRepository,
            DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.apiKeyRepository = apiKeyRepository;
                this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String execute(RegenerateApiKeyCommand command) {
        log.info("Executing RegenerateApiKeyUseCase");

        if (command.environment() == ApiEnvironment.LIVE) {
            Merchant merchant = merchantRepository.findById(command.authenticatedMerchantId())
                    .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));
            
            if (merchant.getComplianceStatus() != com.atlaspay.identity.domain.model.ComplianceStatus.APPROVED) {
                throw new BusinessRuleException(IdentityErrorCode.LIVE_KEYS_REQUIRE_COMPLIANCE_APPROVED, "Live keys require compliance to be approved");
            }
        }

        apiKeyRepository.findByMerchantIdAndKeyTypeAndEnvironmentAndActiveTrue(
                command.authenticatedMerchantId(), command.keyType(), command.environment()
        ).ifPresent(existingKey -> {
            existingKey.revoke();
            apiKeyRepository.save(existingKey);
            publishEvents(existingKey, eventPublisher);
        });

        String envPrefix = command.environment() == ApiEnvironment.LIVE ? "live_" : "test_";
        String typePrefix = command.keyType() == KeyType.PUBLIC ? "pk_" : "sk_";
        String prefix = typePrefix + envPrefix;
        
        String rawKey = prefix + UUID.randomUUID().toString().replace("-", "");

        String keyHash;
        String displayValue;

        if (command.keyType() == KeyType.PUBLIC) {
            keyHash = rawKey;
            displayValue = rawKey;
        } else {
            keyHash = com.atlaspay.shared.util.HashingUtils.sha256Hex(rawKey);
            displayValue = prefix + "****" + rawKey.substring(rawKey.length() - 4);
        }

        ApiKey newKey = new ApiKey(apiKeyRepository.nextIdentity(),
                command.authenticatedMerchantId(),
                command.keyType(),
                command.environment(),
                keyHash,
                displayValue,
                prefix
        );

        apiKeyRepository.save(newKey);
        publishEvents(newKey, eventPublisher);

        return rawKey;
    }
}




