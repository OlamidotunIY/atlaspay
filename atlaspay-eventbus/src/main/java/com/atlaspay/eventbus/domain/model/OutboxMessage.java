package com.atlaspay.eventbus.domain.model;

import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.domain.id.OutboxMessageId;
import java.time.ZonedDateTime;

public class OutboxMessage extends AggregateRoot<OutboxMessageId> {
    private final OutboxMessageId id;
    private final String topic;
    private final String payload;
    private OutboxStatus status;
    private final ZonedDateTime createdAt;
    private ZonedDateTime processedAt;
    
    // For mapping from DB
    public OutboxMessage(OutboxMessageId id, String topic, String payload, OutboxStatus status, ZonedDateTime createdAt, ZonedDateTime processedAt) {
        this.id = id;
        this.topic = topic;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public OutboxMessage(OutboxMessageId id, String topic, String payload) {
        this.id = id != null ? id : OutboxMessageId.generate();
        this.topic = topic;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = ZonedDateTime.now();
    }

    public void markAsSent() {
        this.status = OutboxStatus.SENT;
        this.processedAt = ZonedDateTime.now();
    }

    public void markAsFailed() {
        this.status = OutboxStatus.FAILED;
        this.processedAt = ZonedDateTime.now();
    }

    @Override
    public OutboxMessageId getId() { return id; }
    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
    public OutboxStatus getStatus() { return status; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getProcessedAt() { return processedAt; }
}
