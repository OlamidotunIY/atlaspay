package com.atlaspay.auth.infrastructure.mapper;

import com.atlaspay.auth.domain.model.Session;
import com.atlaspay.auth.infrastructure.entity.SessionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public SessionJpaEntity toEntity(Session domain) {
        if (domain == null) return null;

        return SessionJpaEntity.builder()
                .id(domain.getId())
                .authAccountId(domain.getAuthAccountId())
                .principalId(domain.getPrincipalId())
                .principalType(domain.getPrincipalType())
                .token(domain.getToken())
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .expiresAt(domain.getExpiresAt())
                .revokedAt(domain.getRevokedAt())
                .build();
    }

    public Session toDomain(SessionJpaEntity entity) {
        if (entity == null) return null;

        return new Session(
                entity.getId(),
                entity.getAuthAccountId(),
                entity.getPrincipalId(),
                entity.getPrincipalType(),
                entity.getToken(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getExpiresAt(),
                entity.getStatus()
        );
    }
}
