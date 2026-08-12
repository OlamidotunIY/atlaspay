package com.atlaspay.shared.tracing;

import org.slf4j.MDC;
import java.util.UUID;

/**
 * Utility for managing the Correlation ID used for distributed tracing.
 */
public final class CorrelationId {
    private static final String MDC_KEY = "correlationId";

    private CorrelationId() {}

    /**
     * Retrieves the current correlation ID, generating a new one if missing.
     */
    public static String getOrCreate() {
        String id = MDC.get(MDC_KEY);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            MDC.put(MDC_KEY, id);
        }
        return id;
    }

    /**
     * Sets a specific correlation ID.
     */
    public static void set(String id) {
        MDC.put(MDC_KEY, id);
    }

    /**
     * Clears the current correlation ID.
     */
    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
