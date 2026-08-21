package com.atlaspay.auth.infrastructure.repository;

import com.atlaspay.auth.infrastructure.entity.VerificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataVerificationRepository extends JpaRepository<VerificationJpaEntity, Long> {
    Optional<VerificationJpaEntity> findByIdentifier(String identifier);
}
