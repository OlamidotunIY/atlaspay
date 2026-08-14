package com.atlaspay.identity.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlaspay.identity.application.command.GenerateTestApiKeyPairCommand;

import com.atlaspay.shared.usecase.BaseUseCase;

import com.atlaspay.identity.application.dto.ApiKeyPairResult;
import com.atlaspay.identity.application.port.PasswordEncoder;
import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.identity.domain.repository.ApiKeyRepository;
import com.atlaspay.shared.event.DomainEventPublisher;

import java.util.UUID;

public class GenerateTestApiKeyPairUseCase extends BaseUseCase<GenerateTestApiKeyPairCommand, ApiKeyPairResult> {
    private static final Logger log = LoggerFactory.getLogger(GenerateTestApiKeyPairUseCase.class);


    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher eventPublisher;

    public GenerateTestApiKeyPairUseCase(ApiKeyRepository apiKeyRepository, PasswordEncoder passwordEncoder, DomainEventPublisher eventPublisher) {
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ApiKeyPairResult execute(GenerateTestApiKeyPairCommand command) {
        log.info("Executing GenerateTestApiKeyPairUseCase");

        String rawPublicKey = "pk_test_" + UUID.randomUUID().toString().replace("-", "");
        String rawSecretKey = "sk_test_" + UUID.randomUUID().toString().replace("-", "");

        ApiKey publicKey = new ApiKey(apiKeyRepository.nextIdentity(),
            command.merchantId(),
            KeyType.PUBLIC,
            ApiEnvironment.TEST,
            rawPublicKey,
            rawPublicKey,
            "pk_test_"
        );

        String secretKeyHash = com.atlaspay.shared.util.HashingUtils.sha256Hex(rawSecretKey);
        String secretDisplay = "sk_test_****" + rawSecretKey.substring(rawSecretKey.length() - 4);

        ApiKey secretKey = new ApiKey(apiKeyRepository.nextIdentity(),
            command.merchantId(),
            KeyType.SECRET,
            ApiEnvironment.TEST,
            secretKeyHash,
            secretDisplay,
            "sk_test_"
        );

        apiKeyRepository.save(publicKey);
        apiKeyRepository.save(secretKey);

        publishEvents(publicKey, eventPublisher);
        publishEvents(secretKey, eventPublisher);

        return new ApiKeyPairResult(rawPublicKey, rawSecretKey);
    }
}
