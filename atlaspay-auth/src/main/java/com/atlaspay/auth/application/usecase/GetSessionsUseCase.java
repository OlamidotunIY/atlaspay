package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.dto.SessionDto;
import com.atlaspay.auth.application.query.GetSessionsQuery;
import com.atlaspay.auth.domain.model.Session;
import com.atlaspay.auth.domain.repository.SessionRepository;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetSessionsUseCase extends BaseUseCase<GetSessionsQuery, ApiResponse<List<SessionDto>>> {

    private final SessionRepository sessionRepository;

    public GetSessionsUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<SessionDto>> execute(GetSessionsQuery input) {
        List<Session> sessions = sessionRepository.findByAuthAccountId(input.authAccountId());
        
        List<SessionDto> dtos = sessions.stream()
                .map(s -> new SessionDto(
                        s.getId(),
                        s.getToken(),
                        s.getIpAddress(),
                        s.getUserAgent(),
                        s.getStatus(),
                        s.getCreatedAt(),
                        s.getExpiresAt(),
                        s.getRevokedAt()
                ))
                .toList();

        return new ApiResponse<>(true, "Sessions retrieved successfully", dtos, null);
    }
}
