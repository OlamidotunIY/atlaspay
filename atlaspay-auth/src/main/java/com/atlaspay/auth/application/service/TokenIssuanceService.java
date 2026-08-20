package com.atlaspay.auth.application.service;

import com.atlaspay.auth.application.dto.AuthTokenDto;
import com.atlaspay.auth.application.port.out.TokenGeneratorPort;
import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.model.Session;
import com.atlaspay.auth.domain.repository.AuthAccountRepository;
import com.atlaspay.auth.domain.repository.SessionRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenIssuanceService {

    private final AuthAccountRepository authAccountRepository;
    private final SessionRepository sessionRepository;
    private final TokenGeneratorPort tokenGeneratorPort;
    private final DomainEventPublisher eventPublisher;

    public TokenIssuanceService(
            AuthAccountRepository authAccountRepository,
            SessionRepository sessionRepository,
            TokenGeneratorPort tokenGeneratorPort,
            DomainEventPublisher eventPublisher) {
        this.authAccountRepository = authAccountRepository;
        this.sessionRepository = sessionRepository;
        this.tokenGeneratorPort = tokenGeneratorPort;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuthTokenDto issueTokensAndCreateSession(AuthAccount authAccount, String ipAddress, String userAgent) {
        TokenGeneratorPort.TokenData access = tokenGeneratorPort.generateAccessToken(authAccount.getPrincipalId(), authAccount.getPrincipalType().name(), authAccount.getScope());
        TokenGeneratorPort.TokenData refresh = tokenGeneratorPort.generateRefreshToken(authAccount.getPrincipalId(), authAccount.getPrincipalType().name(), access.jti());

        authAccount.issueTokens(access.token(), refresh.token(), access.expiresAt(), refresh.expiresAt());
        authAccountRepository.save(authAccount);
        
        authAccount.pullDomainEvents().forEach(event -> 
            eventPublisher.publish(EnvelopedDomainEvent.wrap(event))
        );

        Session session = Session.create(
                sessionRepository.nextIdentity(),
                authAccount.getId(),
                authAccount.getPrincipalId(),
                authAccount.getPrincipalType(),
                access.jti(),
                ipAddress,
                userAgent,
                refresh.expiresAt()
        );
        
        sessionRepository.save(session);
        
        session.pullDomainEvents().forEach(event -> 
            eventPublisher.publish(EnvelopedDomainEvent.wrap(event))
        );

        return new AuthTokenDto(access.token(), refresh.token(), access.expiresAt(), refresh.expiresAt());
    }
}
