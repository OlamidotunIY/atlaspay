package com.atlaspay.identity.domain.repository;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.shared.domain.id.ApiKeyId;
import com.atlaspay.shared.domain.id.MerchantId;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository {
    ApiKey save(ApiKey key);
    Optional<ApiKey> findById(ApiKeyId id);
    Optional<ApiKey> findByKeyHash(String keyHash);
    Optional<ApiKey> findByMerchantIdAndKeyTypeAndEnvironmentAndActiveTrue(MerchantId merchantId, KeyType keyType, ApiEnvironment environment);
    List<ApiKey> findAllByMerchantId(MerchantId merchantId);
}
