package com.atlaspay.auth.domain.model;

import com.atlaspay.auth.domain.event.AuthAccountCreatedEvent;
import com.atlaspay.auth.domain.event.AuthAccountSuspendedEvent;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.ConflictException;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class AuthAccount extends AggregateRoot<Long> {

    private final Long id;
    private final Long principalId;
    private final PrincipalType principalType;
    private AuthProvider provider;
    private String credentialHash;
    private String scope;
    private String accessToken;
    private String refreshToken;
    private ZonedDateTime accessTokenExpiresAt;
    private ZonedDateTime refreshTokenExpiresAt;
    private String totpSecret;
    private Boolean totpEnabled;
    private AuthStatus status;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public AuthAccount(Long id, Long principalId, PrincipalType principalType, AuthProvider provider, String credentialHash, String scope, AuthStatus status) {
        this.id = id;
        this.principalId = principalId;
        this.principalType = principalType;
        this.provider = provider;
        this.credentialHash = credentialHash;
        this.scope = scope;
        this.status = status;
        this.totpEnabled = false;
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    public static AuthAccount create(Long id, Long principalId, PrincipalType principalType, AuthProvider provider, String credentialHash, String scope) {
        AuthAccount authAccount = new AuthAccount(id, principalId, principalType, provider, credentialHash, scope, AuthStatus.ACTIVE);

        authAccount.registerEvent(
                new AuthAccountCreatedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(id),
                        ZonedDateTime.now(),
                        null
                ));
        return authAccount;
    }

    public void issueTokens(String accessToken, String refreshToken, ZonedDateTime accessExp, ZonedDateTime refreshExp) {
        if (this.status != AuthStatus.ACTIVE) {
            throw new BusinessRuleException(AuthErrorCode.ACCOUNT_NOT_ACTIVE, "This account is not active");
        }

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessExp;
        this.refreshTokenExpiresAt = refreshExp;
        this.updatedAt = ZonedDateTime.now();
    }

    public void enableTotp(String secret) {
        if (Boolean.TRUE.equals(this.totpEnabled)) {
            throw new ConflictException(AuthErrorCode.TOTP_ALREADY_ENABLED, "TOTP is already enabled for this account");
        }

        this.totpEnabled = true;
        this.totpSecret = secret;
        this.updatedAt = ZonedDateTime.now();
    }

    public void updateCredential(String newHash) {
        this.credentialHash = newHash;
        this.updatedAt = ZonedDateTime.now();
    }

    public void suspend() {
        if (this.status == AuthStatus.SUSPENDED) {
            throw new ConflictException(AuthErrorCode.ACCOUNT_SUSPENDED, "This account is already suspended");
        }

        this.status = AuthStatus.SUSPENDED;
        this.updatedAt = ZonedDateTime.now();

        this.registerEvent(
                new AuthAccountSuspendedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(id),
                        ZonedDateTime.now(),
                        null
                ));
    }

    public void lock() {
        this.status = AuthStatus.LOCKED;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
