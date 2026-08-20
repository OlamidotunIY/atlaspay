package com.atlaspay.auth.domain.model;

import com.atlaspay.auth.domain.event.SessionCreatedEvent;
import com.atlaspay.auth.domain.event.SessionPayload;
import com.atlaspay.auth.domain.event.SessionRevokedEvent;
import com.atlaspay.shared.domain.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Session extends AggregateRoot<Long> {

    private final Long id;
    private final Long authAccountId;
    private final Long principalId;
    private final PrincipalType principalType;
    private final String token;
    private final String ipAddress;
    private final String userAgent;
    private SessionStatus status;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime expiresAt;
    private ZonedDateTime revokedAt;

    public Session(Long id, Long authAccountId, Long principalId, PrincipalType principalType, String token, String ipAddress, String userAgent, ZonedDateTime expiresAt, SessionStatus status) {
        this.id = id;
        this.authAccountId = authAccountId;
        this.principalId = principalId;
        this.principalType = principalType;
        this.token = token;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.expiresAt = expiresAt;
        this.status = status;
        this.createdAt = ZonedDateTime.now();
    }

    public static Session create(Long id, Long authAccountId, Long principalId, PrincipalType principalType, String jti, String ipAddress, String userAgent, ZonedDateTime expiresAt) {
        Session session = new Session(id, authAccountId, principalId, principalType, jti, ipAddress, userAgent, expiresAt, SessionStatus.ACTIVE);
        
        session.registerEvent(
                new SessionCreatedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(session.getId()),
                        ZonedDateTime.now(),
                        new SessionPayload(jti, expiresAt)
                ));
        return session;
    }

    public void revoke() {
        if (this.status != SessionStatus.REVOKED) {
            this.status = SessionStatus.REVOKED;
            this.revokedAt = ZonedDateTime.now();
            
            this.registerEvent(
                new SessionRevokedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(this.getId()),
                        ZonedDateTime.now(),
                        new SessionPayload(this.token, this.expiresAt)
                ));
        }
    }

    public void expire() {
        if (this.status == SessionStatus.ACTIVE) {
            this.status = SessionStatus.EXPIRED;
        }
    }

    @Override
    public Long getId() {
        return id;
    }
}
