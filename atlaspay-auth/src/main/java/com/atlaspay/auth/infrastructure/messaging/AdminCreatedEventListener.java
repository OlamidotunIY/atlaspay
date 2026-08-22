package com.atlaspay.auth.infrastructure.messaging;

import com.atlaspay.auth.application.command.CreateAuthAccountCommand;
import com.atlaspay.auth.application.usecase.CreateAuthAccountUseCase;
import com.atlaspay.auth.domain.event.AdminCredentialsCreatedEvent;
import com.atlaspay.auth.domain.model.AuthProvider;
import com.atlaspay.auth.domain.model.AuthStatus;
import com.atlaspay.auth.domain.model.PrincipalType;
import com.atlaspay.shared.event.BaseKafkaEventListener;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Component
public class AdminCreatedEventListener extends BaseKafkaEventListener {

    private final CreateAuthAccountUseCase createAuthAccountUseCase;
    private final DomainEventPublisher eventPublisher;

    protected AdminCreatedEventListener(
            ObjectMapper objectMapper,
            CreateAuthAccountUseCase createAuthAccountUseCase,
            DomainEventPublisher eventPublisher) {
        super(objectMapper);
        this.createAuthAccountUseCase = createAuthAccountUseCase;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "admin-events", groupId = "auth-module-admin-group")
    public void onAdminCreated(String messagePayload) {
        processEventIfMatches(messagePayload, "AdminCreated", log, root -> {
            String aggregateId = root.path("aggregateId").asText(null);
            if (aggregateId == null) return;

            Long adminId = Long.valueOf(aggregateId);
            String employeeCode = root.path("payload").path("employeeCode").asText(null);
            String personalEmail = root.path("payload").path("personalEmail").asText(null);

            log.info("Received AdminCreated event for admin {}. Creating auth account...", adminId);

            String tempPassword = generateTempPassword();

            CreateAuthAccountCommand authCommand = new CreateAuthAccountCommand(
                    adminId,
                    PrincipalType.ADMIN,
                    employeeCode,
                    null,
                    AuthProvider.EMAIL,
                    tempPassword,
                    "admin:*",
                    AuthStatus.REQUIRES_PASSWORD_CHANGE
            );

            createAuthAccountUseCase.execute(authCommand);

            AdminCredentialsCreatedEvent event = new AdminCredentialsCreatedEvent(
                    UUID.randomUUID().toString(),
                    String.valueOf(adminId),
                    ZonedDateTime.now(),
                    new AdminCredentialsCreatedEvent.Payload(personalEmail, tempPassword)
            );
            
            eventPublisher.publish(com.atlaspay.shared.event.EnvelopedDomainEvent.wrap(event));
        });
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}