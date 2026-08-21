package com.atlaspay.auth.infrastructure.repository;

import com.atlaspay.auth.infrastructure.entity.AuthAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataAuthAccountRepository extends JpaRepository<AuthAccountJpaEntity, Long> {
    Optional<AuthAccountJpaEntity> findByPrincipalId(Long principalId);
}
