package com.atlaspay.auth.infrastructure.adapter.persistence;

import com.atlaspay.auth.domain.model.Session;
import com.atlaspay.auth.domain.model.SessionStatus;
import com.atlaspay.auth.domain.repository.SessionRepository;
import com.atlaspay.auth.infrastructure.entity.SessionJpaEntity;
import com.atlaspay.auth.infrastructure.mapper.SessionMapper;
import com.atlaspay.auth.infrastructure.repository.SpringDataSessionRepository;
import com.atlaspay.shared.infrastructure.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SessionRepositoryAdapter implements SessionRepository {

    private final SpringDataSessionRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final SessionMapper mapper;

    public SessionRepositoryAdapter(SpringDataSessionRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, SessionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("session_seq");
    }

    @Override
    public Session save(Session session) {
        SessionJpaEntity entity = mapper.toEntity(session);
        jpaRepository.save(entity);
        return session;
    }

    @Override
    public Optional<Session> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Session> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public List<Session> findByAuthAccountIdAndStatus(Long authAccountId, SessionStatus status) {
        return jpaRepository.findByAuthAccountIdAndStatus(authAccountId, status)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Session> findByAuthAccountId(Long authAccountId) {
        return jpaRepository.findByAuthAccountId(authAccountId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Session> saveAll(List<Session> sessions) {
        List<SessionJpaEntity> entities = sessions.stream().map(mapper::toEntity).collect(Collectors.toList());
        jpaRepository.saveAll(entities);
        return sessions;
    }
}
