package com.atlaspay.notifications.infrastructure.messaging;

import com.atlaspay.notifications.application.usecase.SendVerificationEmailUseCase;
import com.atlaspay.notifications.infrastructure.messaging.event.IdentityRegistrationEvent;
import com.atlaspay.notifications.infrastructure.messaging.event.IdentityVerificationResentEvent;
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
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class IdentityEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(IdentityEventListener.class);

    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;
    private final DeadLetterRepository deadLetterRepository;

    public IdentityEventListener(
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
    @KafkaListener(topics = "merchant-events", groupId = "notifications-identity-group")
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
            } catch (JsonProcessingException e) {
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
            } catch (JsonProcessingException e) {
                log.error("Error parsing MerchantEmailVerificationResent payload", e);
            }
        });
    }

    @DltHandler
    public void handleDeadLetter(String message, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error) {
        log.error("IdentityEvent permanently failed after all retries and is now in DLQ. Reason: {}", error);
        deadLetterRepository.save(
                UUID.randomUUID().toString(),
                "merchant-events",
                message,
                error,
                ZonedDateTime.now()
        );
    }
}
