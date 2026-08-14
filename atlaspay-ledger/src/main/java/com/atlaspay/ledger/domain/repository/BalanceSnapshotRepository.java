package com.atlaspay.ledger.domain.repository;

import com.atlaspay.ledger.domain.model.BalanceSnapshot;
import java.util.Optional;

public interface BalanceSnapshotRepository {
    Long nextIdentity();
    Optional<BalanceSnapshot> findLatestByAccountId(Long accountId);
    BalanceSnapshot save(BalanceSnapshot snapshot);
}
