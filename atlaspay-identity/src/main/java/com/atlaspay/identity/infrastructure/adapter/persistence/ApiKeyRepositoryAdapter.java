package com.atlaspay.identity.infrastructure.adapter.persistence;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.identity.domain.repository.ApiKeyRepository;
import com.atlaspay.identity.infrastructure.persistence.entity.ApiKeyJpaEntity;
import com.atlaspay.identity.infrastructure.persistence.repository.SpringDataApiKeyRepository;
import com.atlaspay.shared.domain.id.ApiKeyId;
import com.atlaspay.shared.domain.id.MerchantId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ApiKeyRepositoryAdapter implements ApiKeyRepository {

    private final SpringDataApiKeyRepository jpaRepository;

    public ApiKeyRepositoryAdapter(SpringDataApiKeyRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ApiKey save(ApiKey key) {
        ApiKeyJpaEntity entity = toEntity(key);
        jpaRepository.save(entity);
        return key;
    }

    @Override
    public Optional<ApiKey> findById(ApiKeyId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<ApiKey> findByKeyHash(String keyHash) {
        return jpaRepository.findByKeyHash(keyHash).map(this::toDomain);
    }

    @Override
    public Optional<ApiKey> findByMerchantIdAndKeyTypeAndEnvironmentAndActiveTrue(MerchantId merchantId, KeyType keyType, ApiEnvironment environment) {
        return jpaRepository.findByMerchantIdAndKeyTypeAndEnvironmentAndActiveTrue(merchantId.value(), keyType, environment).map(this::toDomain);
    }

    @Override
    public List<ApiKey> findAllByMerchantId(MerchantId merchantId) {
        return jpaRepository.findAllByMerchantId(merchantId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ApiKeyJpaEntity toEntity(ApiKey domain) {
        ApiKeyJpaEntity entity = new ApiKeyJpaEntity();
        entity.setId(domain.getId().value());
        entity.setMerchantId(domain.getMerchantId().value());
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
                new ApiKeyId(entity.getId()),
                new MerchantId(entity.getMerchantId()),
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
