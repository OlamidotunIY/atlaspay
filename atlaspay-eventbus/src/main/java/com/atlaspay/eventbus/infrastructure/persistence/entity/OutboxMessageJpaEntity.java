package com.atlaspay.eventbus.infrastructure.persistence.entity;

import com.atlaspay.eventbus.domain.model.OutboxStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "outbox_messages", indexes = {
    @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
public class OutboxMessageJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)", nullable = false)
    private String id;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "processed_at")
    private ZonedDateTime processedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    protected OutboxMessageJpaEntity() {}

    public OutboxMessageJpaEntity(String id, String topic, String payload, OutboxStatus status, ZonedDateTime createdAt, ZonedDateTime processedAt) {
        this.id = id;
        this.topic = topic;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

}
