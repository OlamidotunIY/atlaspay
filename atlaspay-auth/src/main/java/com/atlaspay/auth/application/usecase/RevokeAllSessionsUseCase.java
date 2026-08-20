package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.RevokeAllSessionsCommand;
import com.atlaspay.auth.domain.model.Session;
import com.atlaspay.auth.domain.model.SessionStatus;
import com.atlaspay.auth.domain.repository.SessionRepository;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RevokeAllSessionsUseCase extends BaseUseCase<RevokeAllSessionsCommand, ApiResponse<Void>> {

    private final SessionRepository sessionRepository;
    private final DomainEventPublisher eventPublisher;

    public RevokeAllSessionsUseCase(SessionRepository sessionRepository, DomainEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<Void> execute(RevokeAllSessionsCommand input) {
        List<Session> activeSessions = sessionRepository.findByAuthAccountIdAndStatus(input.authAccountId(), SessionStatus.ACTIVE);
        
        for (Session session : activeSessions) {
            session.revoke();
            publishEvents(session, eventPublisher);
        }
        
        sessionRepository.saveAll(activeSessions);
        
        return new ApiResponse<>(true, "All active sessions revoked successfully", null, null);
    }
}
