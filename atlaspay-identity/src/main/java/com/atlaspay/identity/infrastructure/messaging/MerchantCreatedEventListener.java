package com.atlaspay.identity.infrastructure.messaging;

import com.atlaspay.identity.application.command.GenerateTestApiKeyPairCommand;
import com.atlaspay.identity.application.usecase.GenerateTestApiKeyPairUseCase;
import com.atlaspay.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MerchantCreatedEventListener extends BaseKafkaEventListener {

    private final GenerateTestApiKeyPairUseCase generateTestApiKeyPairUseCase;

    protected MerchantCreatedEventListener(ObjectMapper objectMapper, GenerateTestApiKeyPairUseCase generateTestApiKeyPairUseCase) {
        super(objectMapper);
        this.generateTestApiKeyPairUseCase = generateTestApiKeyPairUseCase;
    }

    @KafkaListener(topics = "merchant-events", groupId = "identity-module-group")
    public void onMerchantRegistered(String messagePayload) {
        processEventIfMatches(messagePayload, "MerchantRegistered", log, root -> {
            String aggregateId = root.path("aggregateId").asText(null);
            if (aggregateId == null) return;

            Long integration = Long.valueOf(aggregateId);

            log.warn("Received MerchantRegistered event for merchant {}. Creating test api keys for merchant...", integration);

            generateTestApiKeyPairUseCase.execute(new GenerateTestApiKeyPairCommand(integration));

            log.info("Successfully created test api key pair for merchant {}", integration);
        });
    }
}
