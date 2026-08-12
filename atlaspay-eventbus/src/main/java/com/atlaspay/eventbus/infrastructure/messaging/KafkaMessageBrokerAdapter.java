package com.atlaspay.eventbus.infrastructure.messaging;

import com.atlaspay.eventbus.application.port.MessageBrokerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageBrokerAdapter implements MessageBrokerPort {
    
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageBrokerAdapter.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaMessageBrokerAdapter(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topic, String key, String payload) {
        log.debug("Sending message to topic {}: {}", topic, payload);
        kafkaTemplate.send(topic, key, payload);
    }
}
