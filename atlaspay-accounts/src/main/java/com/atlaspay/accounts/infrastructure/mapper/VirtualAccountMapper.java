package com.atlaspay.accounts.infrastructure.mapper;

import com.atlaspay.accounts.domain.model.AccountStatus;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.infrastructure.entity.VirtualAccountEntity;
import com.atlaspay.shared.domain.valueobject.NUBAN;
import com.atlaspay.shared.money.CurrencyCode;
import java.time.ZonedDateTime;
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
                domain.getCurrency().name(),
                0, // version
                ZonedDateTime.now(), // createdAt
                ZonedDateTime.now() // updatedAt
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
                CurrencyCode.valueOf(entity.getCurrency()),
                AccountStatus.valueOf(entity.getStatus()),
                entity.getNuban() != null ? new NUBAN(entity.getNuban()) : null
        );
        return account;
    }
}
