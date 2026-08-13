package com.atlaspay.accounts.infrastructure.messaging;

import com.atlaspay.accounts.application.command.ForceCloseAccountsCommand;
import com.atlaspay.accounts.application.usecase.ForceCloseAccountsUseCase;
import com.atlaspay.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MerchantBannedEventListener extends BaseKafkaEventListener {

    private final ForceCloseAccountsUseCase forceCloseAccountsUseCase;

    public MerchantBannedEventListener(ForceCloseAccountsUseCase forceCloseAccountsUseCase, ObjectMapper objectMapper) {
        super(objectMapper);
        this.forceCloseAccountsUseCase = forceCloseAccountsUseCase;
    }

    @KafkaListener(topics = "merchant-events", groupId = "accounts-module-group")
    public void onMerchantBanned(String messagePayload) {
        processEventIfMatches(messagePayload, "MerchantBanned", log, root -> {
            String aggregateId = root.path("aggregateId").asText(null);
            if (aggregateId == null) {
                return;
            }

            Long integration = Long.valueOf(aggregateId);
            
            log.warn("Received MerchantBanned event for integration {}. Forcing closure of all virtual accounts...", integration);

            forceCloseAccountsUseCase.execute(new ForceCloseAccountsCommand(integration));
            
            log.info("Successfully requested closure for all virtual accounts belonging to integration {}", integration);
        });
    }
}
