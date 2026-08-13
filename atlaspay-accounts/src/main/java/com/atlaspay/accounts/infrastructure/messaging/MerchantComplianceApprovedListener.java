package com.atlaspay.accounts.infrastructure.messaging;

import com.atlaspay.accounts.application.command.IssueVirtualAccountCommand;
import com.atlaspay.accounts.application.usecase.IssueVirtualAccountUseCase;
import com.atlaspay.accounts.domain.model.OwnerType;
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

    @KafkaListener(topics = "merchant-events", groupId = "accounts-module-group")
    public void onMerchantComplianceApproved(String messagePayload) {
        // In a real system, this string would be deserialized into a DTO.
        // Assuming we deserialized it and confirmed the event type is MerchantComplianceApproved
        // and extracted the merchantId (aggregateId).
        
        // For demonstration, let's assume we extracted merchantId:
        String merchantId = extractMerchantId(messagePayload);
        if (merchantId == null) return;
        
        log.info("Received compliance approval for merchant: {}. Issuing virtual accounts...", merchantId);

        // 1. Issue Wema Account
        issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                UUID.randomUUID().toString(),
                merchantId,
                OwnerType.MERCHANT,
                "Wema"
        ));
        
        // 2. Issue Zenith Account
        issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                UUID.randomUUID().toString(),
                merchantId,
                OwnerType.MERCHANT,
                "Zenith"
        ));
    }

    private String extractMerchantId(String payload) {
        // Simplistic parsing for the mock. Real code uses ObjectMapper.
        if (payload.contains("\"eventType\":\"MerchantComplianceApproved\"")) {
            // parse JSON... (Mocking for now)
            return "mer_mock123";
        }
        return null;
    }
}
