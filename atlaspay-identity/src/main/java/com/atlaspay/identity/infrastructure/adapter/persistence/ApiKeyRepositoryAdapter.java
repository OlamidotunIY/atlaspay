package com.atlaspay.identity.infrastructure.adapter.persistence;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.identity.domain.repository.ApiKeyRepository;
import com.atlaspay.identity.infrastructure.entity.ApiKeyJpaEntity;
import com.atlaspay.identity.infrastructure.repository.SpringDataApiKeyRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ApiKeyRepositoryAdapter implements ApiKeyRepository {

    private final SpringDataApiKeyRepository jpaRepository;
    private final com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator;

    public ApiKeyRepositoryAdapter(SpringDataApiKeyRepository jpaRepository, com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ApiKey save(ApiKey key) {
        ApiKeyJpaEntity entity = toEntity(key);
        jpaRepository.save(entity);
        return key;
    }

    @Override
    public Optional<ApiKey> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ApiKey> findByKeyHash(String keyHash) {
        return jpaRepository.findByKeyHash(keyHash).map(this::toDomain);
    }

    @Override
    public Optional<ApiKey> findByMerchantIdAndKeyTypeAndEnvironmentAndActiveTrue(Long merchantId, KeyType keyType, ApiEnvironment environment) {
        return jpaRepository.findByIntegrationAndKeyTypeAndEnvironmentAndActiveTrue(merchantId, keyType, environment).map(this::toDomain);
    }

    @Override
    public List<ApiKey> findAllByMerchantId(Long merchantId) {
        return jpaRepository.findAllByIntegration(merchantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }


    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("apikey_seq");
    }

    private ApiKeyJpaEntity toEntity(ApiKey domain) {
        ApiKeyJpaEntity entity = new ApiKeyJpaEntity();
        entity.setId(domain.getId());
        entity.setIntegration(domain.getMerchantId());
        entity.setKeyType(domain.getKeyType());
        entity.setEnvironment(domain.getEnvironment());
        entity.setKeyHash(domain.getKeyHash());
        entity.setDisplayValue(domain.getDisplayValue());
        entity.setPrefix(domain.getPrefix());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setRevokedAt(domain.getRevokedAt());
        return entity;
    }

    private ApiKey toDomain(ApiKeyJpaEntity entity) {
        ApiKey key = new ApiKey(
                entity.getId(),
                entity.getIntegration(),
                entity.getKeyType(),
                entity.getEnvironment(),
                entity.getKeyHash(),
                entity.getDisplayValue(),
                entity.getPrefix()
        );
        
        if (!entity.isActive()) {
            key.revoke();
        }
        key.pullDomainEvents();
        return key;
    }
}
