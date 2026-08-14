package com.atlaspay.ledger.domain.repository;

import com.atlaspay.ledger.domain.model.LedgerEntry;
import java.util.List;

public interface LedgerEntryRepository {
    Long nextIdentity();
    List<LedgerEntry> findByAccountIdAndIdGreaterThan(Long accountId, Long lastEntryId);
    List<LedgerEntry> findByAccountId(Long accountId);
}
