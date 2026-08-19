package com.atlaspay.ledger.domain.repository;

import com.atlaspay.ledger.domain.model.LedgerEntry;
import java.util.List;
import com.atlaspay.shared.util.PageResult;

public interface LedgerEntryRepository {
    Long nextIdentity();
    List<LedgerEntry> findByAccountIdAndIdGreaterThan(Long accountId, Long lastEntryId);
    List<LedgerEntry> findByAccountId(Long accountId);
    PageResult<LedgerEntry> findByAccountIds(List<Long> accountIds, int page, int perPage);
}
