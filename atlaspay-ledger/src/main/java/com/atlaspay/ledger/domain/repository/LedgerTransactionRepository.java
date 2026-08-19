package com.atlaspay.ledger.domain.repository;

import com.atlaspay.ledger.domain.model.LedgerTransaction;

public interface LedgerTransactionRepository {
    Long nextIdentity();
    LedgerTransaction save(LedgerTransaction transaction);
    boolean existsByReference(String transactionId, String sourceSystem);
}
