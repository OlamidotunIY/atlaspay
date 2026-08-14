package com.atlaspay.notifications.infrastructure.messaging;

import com.atlaspay.notifications.infrastructure.messaging.event.VirtualAccountActivatedNotificationEvent;
import com.atlaspay.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.atlaspay.shared.infrastructure.dlq.DeadLetterRepository;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class AccountEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AccountEventListener.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final DeadLetterRepository deadLetterRepository;

    public AccountEventListener(
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper,
            DeadLetterRepository deadLetterRepository
    ) {
        super(objectMapper);
        this.messagingTemplate = messagingTemplate;
        this.deadLetterRepository = deadLetterRepository;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 5000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "account-events", groupId = "notifications-account-group")
    public void handleAccountEvent(String message) {
        processEventIfMatches(message, "VirtualAccountActivatedEvent", log, root -> {
            try {
                VirtualAccountActivatedNotificationEvent event =
                        objectMapper.treeToValue(root, VirtualAccountActivatedNotificationEvent.class);

                if (event != null && event.payload() != null && event.payload().integration() != null) {
                    String merchantId = String.valueOf(event.payload().integration());
                    messagingTemplate.convertAndSendToUser(
                            merchantId,
                            "/queue/notifications",
                            event
                    );
                    log.info("Pushed VirtualAccountActivated notification to WebSocket for merchant: {}", merchantId);
                }
            } catch (JsonProcessingException e) {
                log.error("Error parsing VirtualAccountActivatedEvent payload", e);
            }
        });
    }

    @DltHandler
    public void handleDeadLetter(String message, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error) {
        log.error("AccountEvent permanently failed after all retries and is now in DLQ. Reason: {}", error);
        deadLetterRepository.save(
                UUID.randomUUID().toString(),
                "account-events",
                message,
                error,
                ZonedDateTime.now()
        );
    }
}
