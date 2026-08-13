package com.atlaspay.shared.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.util.function.Consumer;

public abstract class BaseKafkaEventListener {

    protected final ObjectMapper objectMapper;
    
    protected BaseKafkaEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses the raw Kafka message, checks if it matches the expected event type,
     * and if so, invokes the provided handler with the parsed JsonNode.
     *
     * @param messagePayload The raw JSON string from Kafka
     * @param expectedEventType The class name or event type string to match (e.g. "MerchantComplianceApproved")
     * @param log The logger of the concrete subclass
     * @param action The action to execute with the parsed root JsonNode
     */
    protected void processEventIfMatches(String messagePayload, String expectedEventType, Logger log, Consumer<JsonNode> action) {
        try {
            // Fast fail with string matching before parsing
            if (!messagePayload.contains("\"" + expectedEventType + "\"") && 
                !messagePayload.contains("\"eventType\":\"" + expectedEventType + "\"")) {
                return;
            }

            JsonNode root = objectMapper.readTree(messagePayload);
            String actualType = root.path("eventType").asText(null);

            // Double check post-parsing (if eventType field exists)
            if (actualType != null && !actualType.endsWith(expectedEventType)) {
                return;
            }

            action.accept(root);
        } catch (Exception e) {
            log.error("Failed to parse or process event. Expected type: {}. Payload: {}", expectedEventType, messagePayload, e);
        }
    }
}
