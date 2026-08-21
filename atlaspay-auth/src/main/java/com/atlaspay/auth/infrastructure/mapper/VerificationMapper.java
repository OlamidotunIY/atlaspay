package com.atlaspay.auth.infrastructure.mapper;

import com.atlaspay.auth.domain.model.Verification;
import com.atlaspay.auth.infrastructure.entity.VerificationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class VerificationMapper {

    public VerificationJpaEntity toEntity(Verification domain) {
        if (domain == null) return null;

        return VerificationJpaEntity.builder()
                .id(domain.getId())
                .authAccountId(domain.getAuthAccountId())
                .identifier(domain.getIdentifier())
                .value(domain.getValue())
                .code(domain.getCode())
                .type(domain.getType())
                .status(domain.getStatus())
                .attempts(domain.getAttempts())
                .maxAttempts(domain.getMaxAttempts())
                .createdAt(domain.getCreatedAt())
                .expiresAt(domain.getExpiresAt())
                .verifiedAt(domain.getVerifiedAt())
                .build();
    }

    public Verification toDomain(VerificationJpaEntity entity) {
        if (entity == null) return null;

        return new Verification(
                entity.getId(),
                entity.getAuthAccountId(),
                entity.getIdentifier(),
                entity.getValue(),
                entity.getCode(),
                entity.getType(),
                entity.getStatus(),
                entity.getExpiresAt(),
                entity.getAttempts(),
                entity.getMaxAttempts()
        );
    }
}
