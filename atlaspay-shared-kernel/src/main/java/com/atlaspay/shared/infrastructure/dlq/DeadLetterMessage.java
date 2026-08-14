package com.atlaspay.shared.infrastructure.dlq;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "dead_letter_messages")
public class DeadLetterMessage {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "exception_message", columnDefinition = "TEXT")
    private String exceptionMessage;

    @Column(name = "occurred_at", nullable = false)
    private ZonedDateTime occurredAt;

    protected DeadLetterMessage() {
        // JPA
    }

    public DeadLetterMessage(String id, String topic, String payload, String exceptionMessage, ZonedDateTime occurredAt) {
        this.id = id;
        this.topic = topic;
        this.payload = payload;
        this.exceptionMessage = exceptionMessage;
        this.occurredAt = occurredAt;
    }
}
