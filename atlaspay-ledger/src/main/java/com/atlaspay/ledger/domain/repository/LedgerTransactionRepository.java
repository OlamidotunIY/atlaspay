package com.atlaspay.ledger.domain.repository;

import com.atlaspay.ledger.domain.model.LedgerTransaction;
import com.atlaspay.ledger.domain.model.SourceSystem;

public interface LedgerTransactionRepository {
    Long nextIdentity();
    LedgerTransaction save(LedgerTransaction transaction);
    boolean existsByReference(String transactionId, SourceSystem sourceSystem);
}
