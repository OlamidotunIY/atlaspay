package com.atlaspay.notifications.infrastructure.messaging;

import com.atlaspay.shared.event.WebhookDeliveryRequestedEvent;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
import com.atlaspay.shared.event.DomainEvent;
import com.atlaspay.shared.infrastructure.dlq.DeadLetterRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import java.time.ZonedDateTime;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class WebhookDeliveryListener {

    private final RestClient restClient;
    private final DeadLetterRepository deadLetterRepository;

    public WebhookDeliveryListener(RestClient.Builder restClientBuilder, DeadLetterRepository deadLetterRepository) {
        this.restClient = restClientBuilder.build();
        this.deadLetterRepository = deadLetterRepository;
    }

    // Automatically handles Retries, Exponential Backoff, and DLQ!
    @RetryableTopic(
            attempts = "5",
            backoff = @Backoff(delay = 5000, multiplier = 2.0, maxDelay = 3600000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "WebhookDeliveryRequested", groupId = "notifications-webhook-group")
    public void handleWebhookDelivery(EnvelopedDomainEvent<WebhookDeliveryRequestedEvent.Payload> envelopedEvent) {
        DomainEvent<WebhookDeliveryRequestedEvent.Payload> event = envelopedEvent.event();
        log.info("Attempting to deliver webhook to {}", event.payload().endpointUrl());
        
        var request = restClient.post()
                .uri(event.payload().endpointUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(event.payload().payloadJson());

        event.payload().headers().forEach(request::header);
                
        request.retrieve().toBodilessEntity();
                
        log.info("Successfully delivered webhook to {}", event.payload().endpointUrl());
    }

    // Handles the final failure (Dead Letter Queue)
    @DltHandler
    public void handleDeadLetter(EnvelopedDomainEvent<WebhookDeliveryRequestedEvent.Payload> envelopedEvent, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error) {
        log.error("Webhook to {} permanently failed after all retries and is now in DLQ. Reason: {}", envelopedEvent.event().payload().endpointUrl(), error);
        deadLetterRepository.save(
                UUID.randomUUID().toString(),
                "WebhookDeliveryRequested",
                envelopedEvent.event().payload().payloadJson(),
                error,
                ZonedDateTime.now()
        );
    }
}
