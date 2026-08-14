package com.atlaspay.identity.infrastructure.mapper;

import com.atlaspay.identity.domain.model.SubAccount;
import com.atlaspay.identity.infrastructure.entity.SubAccountJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SubAccountMapper {

    public SubAccountJpaEntity toEntity(SubAccount domain) {
        if (domain == null) return null;

        return new SubAccountJpaEntity(
                domain.getId(),
                domain.getMerchantId(),
                domain.getBankCode(),
                domain.getAccountNumber(),
                domain.getAccountName(),
                domain.getDescription(),
                domain.isActive(),
                domain.getCreatedAt()
        );
    }

    public SubAccount toDomain(SubAccountJpaEntity entity) {
        if (entity == null) return null;

        return new SubAccount(
                entity.getId(),
                entity.getIntegration(),
                entity.getBankCode(),
                entity.getAccountNumber(),
                entity.getAccountName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}
