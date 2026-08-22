package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.RevokeSessionCommand;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.model.Session;
import com.atlaspay.auth.domain.repository.SessionRepository;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevokeSessionUseCase extends BaseUseCase<RevokeSessionCommand, ApiResponse<Void>> {

    private final SessionRepository sessionRepository;
    private final DomainEventPublisher eventPublisher;

    public RevokeSessionUseCase(SessionRepository sessionRepository, DomainEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<Void> execute(RevokeSessionCommand input) {
        Session session = sessionRepository.findByToken(input.token())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.SESSION_NOT_FOUND, "Session not found"));

        session.revoke();
        sessionRepository.save(session);
        publishEvents(session, eventPublisher);

        return new ApiResponse<>(true, "Session revoked successfully", null, null);
    }
}
