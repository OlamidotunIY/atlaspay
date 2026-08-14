package com.atlaspay.accounts.infrastructure.mapper;

import com.atlaspay.accounts.domain.model.AccountStatus;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.infrastructure.entity.VirtualAccountEntity;
import com.atlaspay.shared.domain.valueobject.NUBAN;
import org.springframework.stereotype.Component;

@Component
public class VirtualAccountMapper {

    public VirtualAccountEntity toEntity(VirtualAccount domain) {
        if (domain == null) return null;

        return new VirtualAccountEntity(
                domain.getId(),
                domain.getIntegration(),
                domain.getCustomerCode(),
                domain.getAccountName(),
                domain.getBankName(),
                domain.getNuban() != null ? domain.getNuban().value() : null,
                domain.getStatus().name(),
                domain.getIdempotencyKey(),
                0, // version
                java.time.ZonedDateTime.now(), // createdAt
                java.time.ZonedDateTime.now() // updatedAt
        );
    }

    public VirtualAccount toDomain(VirtualAccountEntity entity) {
        if (entity == null) return null;

        VirtualAccount account = new VirtualAccount(
                entity.getId(),
                entity.getIntegration(),
                entity.getCustomerCode(),
                entity.getAccountName(),
                entity.getBankName(),
                entity.getIdempotencyKey(),
                AccountStatus.valueOf(entity.getStatus()),
                entity.getNuban() != null ? new NUBAN(entity.getNuban()) : null
        );
        return account;
    }
}
