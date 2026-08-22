package com.atlaspay.notifications.infrastructure.messaging;

import com.atlaspay.shared.event.BaseKafkaEventListener;
import com.atlaspay.shared.infrastructure.dlq.DeadLetterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class AdminCredentialsCreatedEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AdminCredentialsCreatedEventListener.class);

    private final DeadLetterRepository deadLetterRepository;

    public AdminCredentialsCreatedEventListener(
            ObjectMapper objectMapper,
            DeadLetterRepository deadLetterRepository
    ) {
        super(objectMapper);
        this.deadLetterRepository = deadLetterRepository;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 5000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "auth-events", groupId = "notifications-admin-creds-group")
    public void handleAuthEvent(String message) {
        processEventIfMatches(message, "AdminCredentialsCreated", log, root -> {
            String email = root.path("payload").path("personalEmail").asText(null);
            String tempPassword = root.path("payload").path("temporaryPassword").asText(null);
            
            if (email != null && tempPassword != null) {
                // TODO: Call SendWelcomeEmailUseCase when it's available
                log.info("Sent welcome email with temp password to admin at {}", email);
            }
        });
    }

    @DltHandler
    public void handleDeadLetter(String message, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error) {
        log.error("AdminCredentialsCreated event permanently failed after all retries and is now in DLQ. Reason: {}", error);
        deadLetterRepository.save(
                UUID.randomUUID().toString(),
                "auth-events",
                message,
                error,
                ZonedDateTime.now()
        );
    }
}