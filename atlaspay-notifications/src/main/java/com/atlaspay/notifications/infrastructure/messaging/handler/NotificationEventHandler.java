package com.atlaspay.notifications.infrastructure.messaging.handler;

import com.atlaspay.notifications.application.usecase.SendVerificationEmailUseCase;
import com.atlaspay.notifications.infrastructure.messaging.dto.IdentityRegistrationEvent;
import com.atlaspay.notifications.infrastructure.messaging.dto.IdentityVerificationResentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventHandler.class);

    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final ObjectMapper objectMapper;

    public NotificationEventHandler(SendVerificationEmailUseCase sendVerificationEmailUseCase, ObjectMapper objectMapper) {
        this.sendVerificationEmailUseCase = sendVerificationEmailUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "merchant-events", groupId = "notifications-group")
    public void handleMerchantEvent(String message) {
        try {
            // Read the event type from the JSON string (Anti-Corruption Layer parsing)
            if (message.contains("\"MerchantRegistered\"")) {
                IdentityRegistrationEvent event = objectMapper.readValue(message, IdentityRegistrationEvent.class);
                if (event.payload() != null) {
                    sendVerificationEmailUseCase.execute(new SendVerificationEmailUseCase.Input(
                        event.payload().email(),
                        event.payload().verificationCode()
                    ));
                    log.info("Handled MerchantRegistered event for {}", event.payload().email());
                }
            } else if (message.contains("\"MerchantEmailVerificationResent\"")) {
                IdentityVerificationResentEvent event = objectMapper.readValue(message, IdentityVerificationResentEvent.class);
                if (event.payload() != null) {
                    sendVerificationEmailUseCase.execute(new SendVerificationEmailUseCase.Input(
                        event.payload().email(),
                        event.payload().verificationCode()
                    ));
                    log.info("Handled MerchantEmailVerificationResent event for {}", event.payload().email());
                }
            }
        } catch (Exception e) {
            log.error("Failed to process message in NotificationEventHandler: {}", message, e);
        }
    }
}
