package com.atlaspay.notifications.infrastructure.messaging;

import com.atlaspay.notifications.application.usecase.SendVerificationEmailUseCase;
import com.atlaspay.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlaspay.shared.infrastructure.dlq.DeadLetterRepository;
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
public class AuthEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthEventListener.class);

    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;
    private final DeadLetterRepository deadLetterRepository;

    public AuthEventListener(
            SendVerificationEmailUseCase sendVerificationEmailUseCase,
            ObjectMapper objectMapper,
            DeadLetterRepository deadLetterRepository
    ) {
        super(objectMapper);
        this.sendVerificationEmailUseCase = sendVerificationEmailUseCase;
        this.deadLetterRepository = deadLetterRepository;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 5000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "auth-events", groupId = "notifications-auth-group")
    public void handleAuthEvent(String message) {
        processEventIfMatches(message, "VerificationCreated", log, root -> {
            String email = root.path("payload").path("identifier").asText(null);
            String rawCode = root.path("payload").path("rawCode").asText(null);
            
            if (email != null && rawCode != null) {
                sendVerificationEmailUseCase.execute(new SendVerificationEmailUseCase.Input(email, rawCode));
                log.info("Handled VerificationCreated event for {}", email);
            }
        });

        processEventIfMatches(message, "PasswordSetupInitiated", log, root -> {
            String email = root.path("payload").path("identifier").asText(null);
            String setupToken = root.path("payload").path("setupToken").asText(null);
            
            if (email != null && setupToken != null) {
                // TODO: Call a usecase to send the password setup link email
                log.info("Handled PasswordSetupInitiated event for {}. Setup link sent.", email);
            }
        });
    }

    @DltHandler
    public void handleDeadLetter(String message, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error) {
        log.error("AuthEvent permanently failed after all retries and is now in DLQ. Reason: {}", error);
        deadLetterRepository.save(
                UUID.randomUUID().toString(),
                "auth-events",
                message,
                error,
                ZonedDateTime.now()
        );
    }
}