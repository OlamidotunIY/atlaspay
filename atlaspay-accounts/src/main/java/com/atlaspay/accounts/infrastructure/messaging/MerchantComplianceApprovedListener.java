package com.atlaspay.accounts.infrastructure.messaging;

import com.atlaspay.accounts.application.command.IssueVirtualAccountCommand;
import com.atlaspay.accounts.application.usecase.IssueVirtualAccountUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantComplianceApprovedListener {

    private final IssueVirtualAccountUseCase issueVirtualAccountUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "merchant-events", groupId = "accounts-module-group")
    public void onMerchantComplianceApproved(String messagePayload) {
        try {
            if (!messagePayload.contains("\"eventType\":\"MerchantComplianceApproved\"") && 
                !messagePayload.contains("\"MerchantComplianceApproved\"")) {
                return;
            }

            JsonNode root = objectMapper.readTree(messagePayload);
            String aggregateId = root.path("aggregateId").asText(null);
            if (aggregateId == null) {
                return;
            }

            Long integrationId = Long.valueOf(aggregateId);
            
            JsonNode payloadNode = root.path("payload");
            String businessName = payloadNode.path("businessName").asText("Main Account");

            log.info("Received compliance approval for merchant {}. Issuing virtual accounts...", integrationId);

            // 1. Issue Wema Account
            issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                    integrationId,
                    "MERCHANT", // Merchant's own account
                    businessName,
                    "Wema Bank",
                    UUID.randomUUID().toString()
            ));

            // 2. Issue Zenith Account
            issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                    integrationId,
                    "MERCHANT", // Merchant's own account
                    businessName,
                    "Zenith Bank",
                    UUID.randomUUID().toString()
            ));
            
        } catch (Exception e) {
            log.error("Failed to process MerchantComplianceApproved event: {}", messagePayload, e);
        }
    }
}
