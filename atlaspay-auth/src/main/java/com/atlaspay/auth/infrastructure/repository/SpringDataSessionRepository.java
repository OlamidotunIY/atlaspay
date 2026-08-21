package com.atlaspay.auth.infrastructure.repository;

import com.atlaspay.auth.infrastructure.entity.SessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataSessionRepository extends JpaRepository<SessionJpaEntity, Long> {
    Optional<SessionJpaEntity> findByToken(String token);
    List<SessionJpaEntity> findByPrincipalId(Long principalId);
    List<SessionJpaEntity> findByAuthAccountId(Long authAccountId);
}
