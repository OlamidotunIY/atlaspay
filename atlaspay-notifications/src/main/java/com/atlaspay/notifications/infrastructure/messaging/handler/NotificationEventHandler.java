package com.atlaspay.notifications.infrastructure.messaging.handler;

import com.atlaspay.notifications.application.usecase.SendVerificationEmailUseCase;
import com.atlaspay.notifications.infrastructure.messaging.dto.IdentityRegistrationEvent;
import com.atlaspay.notifications.infrastructure.messaging.dto.IdentityVerificationResentEvent;
import com.atlaspay.notifications.infrastructure.messaging.dto.VirtualAccountActivatedNotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.atlaspay.shared.event.BaseKafkaEventListener;

@Component
public class NotificationEventHandler extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventHandler.class);

    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public NotificationEventHandler(SendVerificationEmailUseCase sendVerificationEmailUseCase, 
                                    org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate, 
                                    ObjectMapper objectMapper) {
        super(objectMapper);
        this.sendVerificationEmailUseCase = sendVerificationEmailUseCase;
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "merchant-events", groupId = "notifications-group")
    public void handleMerchantEvent(String message) {
        processEventIfMatches(message, "MerchantRegistered", log, root -> {
            try {
                IdentityRegistrationEvent event = objectMapper.treeToValue(root, IdentityRegistrationEvent.class);
                if (event != null && event.payload() != null) {
                    sendVerificationEmailUseCase.execute(new SendVerificationEmailUseCase.Input(
                        event.payload().email(),
                        event.payload().verificationCode()
                    ));
                    log.info("Handled MerchantRegistered event for {}", event.payload().email());
                }
            } catch (Exception e) {
                log.error("Error parsing MerchantRegistered payload", e);
            }
        });

        processEventIfMatches(message, "MerchantEmailVerificationResent", log, root -> {
            try {
                IdentityVerificationResentEvent event = objectMapper.treeToValue(root, IdentityVerificationResentEvent.class);
                if (event != null && event.payload() != null) {
                    sendVerificationEmailUseCase.execute(new SendVerificationEmailUseCase.Input(
                        event.payload().email(),
                        event.payload().verificationCode()
                    ));
                    log.info("Handled MerchantEmailVerificationResent event for {}", event.payload().email());
                }
            } catch (Exception e) {
                log.error("Error parsing MerchantEmailVerificationResent payload", e);
            }
        });
    }
    
    @KafkaListener(topics = "account-events", groupId = "notifications-group")
    public void handleAccountEvent(String message) {
        processEventIfMatches(message, "VirtualAccountActivatedEvent", log, root -> {
            try {
                com.atlaspay.notifications.infrastructure.messaging.dto.VirtualAccountActivatedNotificationEvent event = 
                        objectMapper.treeToValue(root, com.atlaspay.notifications.infrastructure.messaging.dto.VirtualAccountActivatedNotificationEvent.class);
                
                if (event != null && event.payload() != null && event.payload().integration() != null) {
                    String merchantId = String.valueOf(event.payload().integration());
                    messagingTemplate.convertAndSendToUser(
                            merchantId,
                            "/queue/notifications",
                            event
                    );
                    log.info("Pushed VirtualAccountActivated notification to WebSocket for merchant: {}", merchantId);
                }
            } catch (Exception e) {
                log.error("Error parsing VirtualAccountActivatedEvent payload", e);
            }
        });
    }
}
