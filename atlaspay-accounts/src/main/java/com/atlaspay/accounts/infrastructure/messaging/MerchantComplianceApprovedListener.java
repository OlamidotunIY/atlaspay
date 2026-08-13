package com.atlaspay.accounts.infrastructure.messaging;

import com.atlaspay.accounts.application.command.IssueVirtualAccountCommand;
import com.atlaspay.accounts.application.usecase.IssueVirtualAccountUseCase;
import com.atlaspay.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class MerchantComplianceApprovedListener extends BaseKafkaEventListener {

    private final IssueVirtualAccountUseCase issueVirtualAccountUseCase;

    public MerchantComplianceApprovedListener(IssueVirtualAccountUseCase issueVirtualAccountUseCase, ObjectMapper objectMapper) {
        super(objectMapper);
        this.issueVirtualAccountUseCase = issueVirtualAccountUseCase;
    }

    @KafkaListener(topics = "merchant-events", groupId = "accounts-module-group")
    public void onMerchantComplianceApproved(String messagePayload) {
        processEventIfMatches(messagePayload, "MerchantComplianceApproved", log, root -> {
            String aggregateId = root.path("aggregateId").asText(null);
            if (aggregateId == null) {
                return;
            }

            Long integrationId = Long.valueOf(aggregateId);
            
            JsonNode payloadNode = root.path("payload");
            String merchantName = payloadNode.path("merchantName").asText("Main Account");

            log.info("Received compliance approval for merchant {}. Issuing virtual accounts...", integrationId);

            // 1. Issue Wema Account
            issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                    integrationId,
                    null, // Merchant's own account
                    merchantName,
                    "Wema Bank",
                    UUID.randomUUID().toString()
            ));

            // 2. Issue Zenith Account
            issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                    integrationId,
                    null, // Merchant's own account
                    merchantName,
                    "Zenith Bank",
                    UUID.randomUUID().toString()
            ));
        });
    }
}
