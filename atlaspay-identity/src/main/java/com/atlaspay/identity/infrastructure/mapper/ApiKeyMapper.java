package com.atlaspay.identity.infrastructure.mapper;

import com.atlaspay.identity.domain.model.ApiKey;
import com.atlaspay.identity.infrastructure.entity.ApiKeyJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyMapper {

    public ApiKeyJpaEntity toEntity(ApiKey domain) {
        if (domain == null) return null;

        return new ApiKeyJpaEntity(
                domain.getId(),
                domain.getMerchantId(),
                domain.getKeyType(),
                domain.getEnvironment(),
                domain.getKeyHash(),
                domain.getDisplayValue(),
                domain.getPrefix(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getRevokedAt()
        );
    }

    public ApiKey toDomain(ApiKeyJpaEntity entity) {
        if (entity == null) return null;

        return new ApiKey(
                entity.getId(),
                entity.getIntegration(),
                entity.getKeyType(),
                entity.getEnvironment(),
                entity.getKeyHash(),
                entity.getDisplayValue(),
                entity.getPrefix(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getRevokedAt()
        );
    }
}
