package com.atlaspay.auth.infrastructure.mapper;

import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.infrastructure.entity.AuthAccountJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthAccountMapper {

    public AuthAccountJpaEntity toEntity(AuthAccount domain) {
        if (domain == null) return null;

        return AuthAccountJpaEntity.builder()
                .id(domain.getId())
                .principalId(domain.getPrincipalId())
                .principalType(domain.getPrincipalType())
                .provider(domain.getProvider())
                .credentialHash(domain.getCredentialHash())
                .scope(domain.getScope())
                .accessToken(domain.getAccessToken())
                .refreshToken(domain.getRefreshToken())
                .accessTokenExpiresAt(domain.getAccessTokenExpiresAt())
                .refreshTokenExpiresAt(domain.getRefreshTokenExpiresAt())
                .totpSecret(domain.getTotpSecret())
                .totpEnabled(domain.getTotpEnabled() != null ? domain.getTotpEnabled() : false)
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public AuthAccount toDomain(AuthAccountJpaEntity entity) {
        if (entity == null) return null;

        return new AuthAccount(
                entity.getId(),
                entity.getPrincipalId(),
                entity.getPrincipalType(),
                entity.getProvider(),
                entity.getCredentialHash(),
                entity.getScope(),
                entity.getAccessToken(),
                entity.getRefreshToken(),
                entity.getAccessTokenExpiresAt(),
                entity.getRefreshTokenExpiresAt(),
                entity.getTotpSecret(),
                entity.isTotpEnabled(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
