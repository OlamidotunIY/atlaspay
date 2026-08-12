package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.command.GenerateTestApiKeyPairCommand;

import com.atlaspay.shared.usecase.BaseUseCase;

import com.atlaspay.identity.application.dto.ApiKeyPairResult;
import com.atlaspay.identity.application.port.out.PasswordEncoder;
import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.identity.domain.repository.ApiKeyRepository;
import com.atlaspay.shared.domain.id.ApiKeyId;
import com.atlaspay.shared.event.DomainEventPublisher;

import java.util.UUID;

public class GenerateTestApiKeyPairUseCase extends BaseUseCase<GenerateTestApiKeyPairCommand, ApiKeyPairResult> {

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
        String rawPublicKey = "pk_test_" + UUID.randomUUID().toString().replace("-", "");
        String rawSecretKey = "sk_test_" + UUID.randomUUID().toString().replace("-", "");

        ApiKey publicKey = new ApiKey(
            ApiKeyId.generate(),
            command.merchantId(),
            KeyType.PUBLIC,
            ApiEnvironment.TEST,
            rawPublicKey,
            rawPublicKey,
            "pk_test_"
        );

        String secretKeyHash = com.atlaspay.shared.util.HashingUtils.sha256Hex(rawSecretKey);
        String secretDisplay = "sk_test_****" + rawSecretKey.substring(rawSecretKey.length() - 4);

        ApiKey secretKey = new ApiKey(
            ApiKeyId.generate(),
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
