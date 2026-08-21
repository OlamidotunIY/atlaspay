package com.atlaspay.auth.infrastructure.entity;

import com.atlaspay.auth.domain.model.AuthProvider;
import com.atlaspay.auth.domain.model.AuthStatus;
import com.atlaspay.auth.domain.model.PrincipalType;
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
@Table(name = "auth_accounts", indexes = {
        @Index(name = "idx_auth_account_principal", columnList = "principalId, principalType")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AuthAccountJpaEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private Long principalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrincipalType principalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(nullable = false)
    private String credentialHash;

    private String scope;

    private String accessToken;

    private String refreshToken;

    private ZonedDateTime accessTokenExpiresAt;

    private ZonedDateTime refreshTokenExpiresAt;

    private String totpSecret;

    @Column(nullable = false)
    private boolean totpEnabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthStatus status;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;
}

