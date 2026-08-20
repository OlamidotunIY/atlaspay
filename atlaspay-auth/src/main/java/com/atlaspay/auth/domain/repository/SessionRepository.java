package com.atlaspay.auth.domain.repository;

import com.atlaspay.auth.domain.model.Session;
import com.atlaspay.auth.domain.model.SessionStatus;

import java.util.List;
import java.util.Optional;

public interface SessionRepository {
    Long nextIdentity();
    Session save(Session session);
    Optional<Session> findById(Long id);
    Optional<Session> findByToken(String token);
    List<Session> findByAuthAccountIdAndStatus(Long authAccountId, SessionStatus status);
    List<Session> saveAll(List<Session> sessions);
}
