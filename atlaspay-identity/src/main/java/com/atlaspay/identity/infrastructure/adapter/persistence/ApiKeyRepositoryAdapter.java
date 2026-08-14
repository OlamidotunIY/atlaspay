package com.atlaspay.identity.infrastructure.adapter.persistence;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.identity.domain.repository.ApiKeyRepository;
import com.atlaspay.identity.infrastructure.entity.ApiKeyJpaEntity;
import com.atlaspay.identity.infrastructure.mapper.ApiKeyMapper;
import com.atlaspay.identity.infrastructure.repository.SpringDataApiKeyRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ApiKeyRepositoryAdapter implements ApiKeyRepository {

    private final SpringDataApiKeyRepository jpaRepository;
    private final com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator;
    private final ApiKeyMapper mapper;

    public ApiKeyRepositoryAdapter(SpringDataApiKeyRepository jpaRepository, com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator, ApiKeyMapper mapper) {
        this.sequenceGenerator = sequenceGenerator;
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ApiKey save(ApiKey key) {
        ApiKeyJpaEntity entity = mapper.toEntity(key);
        jpaRepository.save(entity);
        return key;
    }

    @Override
    public Optional<ApiKey> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ApiKey> findByKeyHash(String keyHash) {
        return jpaRepository.findByKeyHash(keyHash).map(mapper::toDomain);
    }

    @Override
    public Optional<ApiKey> findByMerchantIdAndKeyTypeAndEnvironmentAndActiveTrue(Long merchantId, KeyType keyType, ApiEnvironment environment) {
        return jpaRepository.findByIntegrationAndKeyTypeAndEnvironmentAndActiveTrue(merchantId, keyType, environment).map(mapper::toDomain);
    }

    @Override
    public List<ApiKey> findAllByMerchantId(Long merchantId) {
        return jpaRepository.findAllByIntegration(merchantId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }


    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("apikey_seq");
    }


}
