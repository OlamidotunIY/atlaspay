package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.command.GenerateLiveApiKeyPairCommand;

import com.atlaspay.shared.usecase.BaseUseCase;

import com.atlaspay.identity.application.dto.ApiKeyPairResult;
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
import com.atlaspay.identity.application.port.PasswordEncoder;

import java.util.UUID;

public class GenerateLiveApiKeyPairUseCase extends BaseUseCase<GenerateLiveApiKeyPairCommand, ApiKeyPairResult> {

    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher eventPublisher;

    public GenerateLiveApiKeyPairUseCase(
            MerchantRepository merchantRepository,
            ApiKeyRepository apiKeyRepository,
            PasswordEncoder passwordEncoder,
            DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ApiKeyPairResult execute(GenerateLiveApiKeyPairCommand command) {
        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        if (merchant.getComplianceStatus() != com.atlaspay.identity.domain.model.ComplianceStatus.APPROVED) {
            throw new BusinessRuleException(IdentityErrorCode.LIVE_KEYS_REQUIRE_COMPLIANCE_APPROVED, "Live keys require compliance to be approved");
        }

        String rawPublicKey = "pk_live_" + UUID.randomUUID().toString().replace("-", "");
        String rawSecretKey = "sk_live_" + UUID.randomUUID().toString().replace("-", "");

        ApiKey publicKey = new ApiKey(apiKeyRepository.nextIdentity(),
                command.merchantId(),
                KeyType.PUBLIC,
                ApiEnvironment.LIVE,
                rawPublicKey,
                rawPublicKey,
                "pk_live_"
        );

        String secretHash = com.atlaspay.shared.util.HashingUtils.sha256Hex(rawSecretKey);
        String secretDisplay = "sk_live_****" + rawSecretKey.substring(rawSecretKey.length() - 4);

        ApiKey secretKey = new ApiKey(apiKeyRepository.nextIdentity(),
                command.merchantId(),
                KeyType.SECRET,
                ApiEnvironment.LIVE,
                secretHash,
                secretDisplay,
                "sk_live_"
        );

        apiKeyRepository.save(publicKey);
        apiKeyRepository.save(secretKey);

        publishEvents(publicKey, eventPublisher);
        publishEvents(secretKey, eventPublisher);

        return new ApiKeyPairResult(rawPublicKey, rawSecretKey);
    }
}
