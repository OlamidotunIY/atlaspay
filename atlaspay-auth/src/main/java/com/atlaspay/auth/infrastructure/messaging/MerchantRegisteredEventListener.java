package com.atlaspay.auth.infrastructure.messaging;

import com.atlaspay.auth.application.command.CreateAuthAccountCommand;
import com.atlaspay.auth.application.command.CreateVerificationCommand;
import com.atlaspay.auth.application.usecase.CreateAuthAccountUseCase;
import com.atlaspay.auth.application.usecase.CreateVerificationUseCase;
import com.atlaspay.auth.domain.model.AuthProvider;
import com.atlaspay.auth.domain.model.AuthStatus;
import com.atlaspay.auth.domain.model.PrincipalType;
import com.atlaspay.auth.domain.model.VerificationType;
import com.atlaspay.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MerchantRegisteredEventListener extends BaseKafkaEventListener {

    private final CreateAuthAccountUseCase createAuthAccountUseCase;
    private final CreateVerificationUseCase createVerificationUseCase;

    protected MerchantRegisteredEventListener(
            ObjectMapper objectMapper,
            CreateAuthAccountUseCase createAuthAccountUseCase,
            CreateVerificationUseCase createVerificationUseCase) {
        super(objectMapper);
        this.createAuthAccountUseCase = createAuthAccountUseCase;
        this.createVerificationUseCase = createVerificationUseCase;
    }

    @KafkaListener(topics = "merchant-events", groupId = "auth-module-group")
    public void onMerchantRegistered(String messagePayload) {
        processEventIfMatches(messagePayload, "MerchantRegistered", log, root -> {
            String aggregateId = root.path("aggregateId").asText(null);
            if (aggregateId == null) return;

            Long merchantId = Long.valueOf(aggregateId);
            String email = root.path("payload").path("email").asText(null);

            log.info("Received MerchantRegistered event for merchant {}. Creating auth account and triggering verification...", merchantId);

            CreateAuthAccountCommand authCommand = new CreateAuthAccountCommand(
                    merchantId,
                    PrincipalType.MERCHANT,
                    email,
                    null, // No secondary identifier yet
                    AuthProvider.EMAIL,
                    null, // No password yet
                    "merchant:*",
                    AuthStatus.PENDING_EMAIL_VERIFICATION
            );

            createAuthAccountUseCase.execute(authCommand);

            CreateVerificationCommand verificationCommand = new CreateVerificationCommand(
                    merchantId,
                    email,
                    VerificationType.EMAIL_VERIFICATION
            );

            createVerificationUseCase.execute(verificationCommand);
        });
    }
}