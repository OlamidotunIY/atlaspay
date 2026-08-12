package com.atlaspay.identity.infrastructure.persistence.repository;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.identity.infrastructure.persistence.entity.ApiKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataApiKeyRepository extends JpaRepository<ApiKeyJpaEntity, String> {
    Optional<ApiKeyJpaEntity> findByKeyHash(String keyHash);
    Optional<ApiKeyJpaEntity> findByMerchantIdAndKeyTypeAndEnvironmentAndActiveTrue(String merchantId, KeyType keyType, ApiEnvironment environment);
    List<ApiKeyJpaEntity> findAllByMerchantId(String merchantId);
}
