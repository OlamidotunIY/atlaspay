package com.atlaspay.ledger.infrastructure.mapper;

import com.atlaspay.ledger.domain.model.BalanceSnapshot;
import com.atlaspay.ledger.infrastructure.entity.BalanceSnapshotJpaEntity;
import com.atlaspay.shared.money.CurrencyCode;
import com.atlaspay.shared.money.Money;
import org.springframework.stereotype.Component;

@Component
public class BalanceSnapshotMapper {

    public BalanceSnapshotJpaEntity toEntity(BalanceSnapshot domain) {
        if (domain == null) return null;

        return new BalanceSnapshotJpaEntity(
                domain.getId(),
                domain.getAccountId(),
                domain.getBalance().amount(),
                domain.getBalance().currency().name(),
                domain.getLastLedgerEntryId(),
                domain.getSnapshotAt()
        );
    }

    public BalanceSnapshot toDomain(BalanceSnapshotJpaEntity entity) {
        if (entity == null) return null;

        return new BalanceSnapshot(
                entity.getId(),
                entity.getAccountId(),
                Money.of(entity.getBalance(), CurrencyCode.valueOf(entity.getCurrency())),
                entity.getLastLedgerEntryId(),
                entity.getCreatedAt()
        );
    }
}
