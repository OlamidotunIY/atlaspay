package com.atlaspay.identity.infrastructure.repository;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.identity.infrastructure.entity.ApiKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataApiKeyRepository extends JpaRepository<ApiKeyJpaEntity, Long> {
    Optional<ApiKeyJpaEntity> findByKeyHash(String keyHash);
    Optional<ApiKeyJpaEntity> findByIntegrationAndKeyTypeAndEnvironmentAndActiveTrue(Long merchantId, KeyType keyType, ApiEnvironment environment);
    List<ApiKeyJpaEntity> findAllByIntegration(Long merchantId);
}
