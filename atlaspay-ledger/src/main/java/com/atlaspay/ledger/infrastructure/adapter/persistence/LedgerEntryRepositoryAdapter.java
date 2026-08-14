package com.atlaspay.ledger.infrastructure.adapter.persistence;

import com.atlaspay.ledger.domain.model.EntryType;
import com.atlaspay.ledger.domain.model.LedgerEntry;
import com.atlaspay.ledger.domain.model.TransactionReference;
import com.atlaspay.ledger.domain.repository.LedgerEntryRepository;
import com.atlaspay.ledger.infrastructure.entity.LedgerEntryJpaEntity;
import com.atlaspay.ledger.domain.repository.LedgerEntryRepository;
import com.atlaspay.ledger.infrastructure.entity.LedgerEntryJpaEntity;
import com.atlaspay.ledger.infrastructure.mapper.LedgerEntryMapper;
import com.atlaspay.ledger.infrastructure.repository.SpringDataLedgerEntryRepository;
import com.atlaspay.shared.infrastructure.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LedgerEntryRepositoryAdapter implements LedgerEntryRepository {

    private final SpringDataLedgerEntryRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final LedgerEntryMapper mapper;

    public LedgerEntryRepositoryAdapter(SpringDataLedgerEntryRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, LedgerEntryMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("ledger_entry_seq");
    }

    @Override
    public List<LedgerEntry> findByAccountIdAndIdGreaterThan(Long accountId, Long lastEntryId) {
        return jpaRepository.findEntriesAfter(accountId, lastEntryId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<LedgerEntry> findByAccountId(Long accountId) {
        return jpaRepository.findByAccountId(accountId).stream()
                .map(mapper::toDomain)
                .toList();
    }


}
