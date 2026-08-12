package com.atlaspay.app.event;

import com.atlaspay.shared.event.EnvelopedDomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class KafkaEventForwarder {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventForwarder.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventForwarder(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void forwardToKafka(EnvelopedDomainEvent<?> envelopedEvent) {
        try {
            // Determine the topic based on the event type. 
            // For now, route all Merchant events to "merchant-events".
            String topic = getTopicForEvent(envelopedEvent.event().getClass().getSimpleName());
            String jsonPayload = objectMapper.writeValueAsString(envelopedEvent.event());
            
            kafkaTemplate.send(topic, envelopedEvent.correlationId(), jsonPayload);
            log.info("Successfully published event {} to Kafka topic {}", envelopedEvent.event().getClass().getSimpleName(), topic);
        } catch (Exception e) {
            log.error("Failed to forward event to Kafka", e);
        }
    }

    private String getTopicForEvent(String eventClassName) {
        if (eventClassName.startsWith("Merchant") || eventClassName.startsWith("ApiKey")) {
            return "merchant-events";
        }
        return "system-events";
    }
}
