package com.atlaspay.identity.infrastructure.persistence.entity;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.KeyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
public class ApiKeyJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false)
    private KeyType keyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false)
    private ApiEnvironment environment;

    @Column(name = "key_hash", nullable = false)
    private String keyHash;

    @Column(name = "display_value", nullable = false)
    private String displayValue;

    @Column(name = "prefix", nullable = false)
    private String prefix;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "revoked_at")
    private ZonedDateTime revokedAt;
}
