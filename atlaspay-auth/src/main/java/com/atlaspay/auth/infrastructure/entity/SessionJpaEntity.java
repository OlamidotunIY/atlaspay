package com.atlaspay.auth.infrastructure.entity;

import com.atlaspay.auth.domain.model.PrincipalType;
import com.atlaspay.auth.domain.model.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "sessions", indexes = {
        @Index(name = "idx_session_token", columnList = "token"),
        @Index(name = "idx_session_auth_account_status", columnList = "authAccountId, status")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SessionJpaEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private Long authAccountId;

    @Column(nullable = false)
    private Long principalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrincipalType principalType;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String ipAddress;

    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime expiresAt;

    private ZonedDateTime revokedAt;
}

