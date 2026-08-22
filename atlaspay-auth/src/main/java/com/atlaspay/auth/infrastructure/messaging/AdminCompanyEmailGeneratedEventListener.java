package com.atlaspay.auth.infrastructure.messaging;

import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.repository.AuthAccountRepository;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
import com.atlaspay.shared.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AdminCompanyEmailGeneratedEventListener {

    private static final Logger log = LoggerFactory.getLogger(AdminCompanyEmailGeneratedEventListener.class);
    private final AuthAccountRepository authAccountRepository;

    public AdminCompanyEmailGeneratedEventListener(AuthAccountRepository authAccountRepository) {
        this.authAccountRepository = authAccountRepository;
    }

    // Local definition of the event payload from the admin module
    public record AdminEmailPayload(
        Long adminId,
        String employeeCode,
        String companyEmail
    ) {}

    @KafkaListener(topics = "atlaspay.admin.AdminCompanyEmailGenerated", groupId = "auth-group")
    public void handle(EnvelopedDomainEvent<AdminEmailPayload> envelopedEvent) {
        AdminEmailPayload payload = envelopedEvent.event().payload();
        log.info("Received AdminCompanyEmailGenerated event for employeeCode: {}", payload.employeeCode());

        AuthAccount authAccount = authAccountRepository.findByIdentifier(payload.employeeCode())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found for admin"));

        authAccount.updateSecondaryIdentifier(payload.companyEmail());
        
        authAccountRepository.save(authAccount);
        log.info("Successfully updated secondaryIdentifier for admin {} to {}", payload.employeeCode(), payload.companyEmail());
    }
}

