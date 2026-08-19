package com.atlaspay.ledger.infrastructure.adapter.persistence;

import com.atlaspay.ledger.domain.model.LedgerTransaction;
import com.atlaspay.ledger.domain.repository.LedgerTransactionRepository;
import com.atlaspay.ledger.infrastructure.entity.LedgerTransactionJpaEntity;
import com.atlaspay.ledger.infrastructure.mapper.LedgerTransactionMapper;
import com.atlaspay.ledger.infrastructure.repository.SpringDataLedgerTransactionRepository;
import com.atlaspay.ledger.domain.model.SourceSystem;
import com.atlaspay.shared.infrastructure.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;

@Repository
public class LedgerTransactionRepositoryAdapter implements LedgerTransactionRepository {

    private final SpringDataLedgerTransactionRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final LedgerTransactionMapper mapper;

    public LedgerTransactionRepositoryAdapter(SpringDataLedgerTransactionRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, LedgerTransactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("ledger_transaction_seq");
    }

    @Override
    public LedgerTransaction save(LedgerTransaction transaction) {
        LedgerTransactionJpaEntity entity = mapper.toEntity(transaction);
        LedgerTransactionJpaEntity savedEntity = jpaRepository.save(entity);

        // Ensure we dispatch domain events via outbox later. For now, aggregate tracks them.
        return transaction;
    }

    @Override
    public boolean existsByReference(String transactionId, SourceSystem sourceSystem) {
        return jpaRepository.existsBySourceTransaction(transactionId, sourceSystem.name());
    }
}
