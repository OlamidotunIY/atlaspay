package com.atlaspay.identity.infrastructure.entity;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.KeyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "api_keys")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiKeyJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "integration", nullable = false)
    private Long integration;

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

    @Setter
    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Setter
    @Column(name = "revoked_at")
    private ZonedDateTime revokedAt;
}
