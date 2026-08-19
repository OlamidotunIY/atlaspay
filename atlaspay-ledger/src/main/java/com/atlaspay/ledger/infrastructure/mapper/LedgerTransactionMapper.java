package com.atlaspay.ledger.infrastructure.mapper;

import com.atlaspay.ledger.domain.model.LedgerTransaction;
import com.atlaspay.ledger.infrastructure.entity.LedgerEntryJpaEntity;
import com.atlaspay.ledger.infrastructure.entity.LedgerTransactionJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LedgerTransactionMapper {

    private final LedgerEntryMapper entryMapper;

    public LedgerTransactionMapper(LedgerEntryMapper entryMapper) {
        this.entryMapper = entryMapper;
    }

    public LedgerTransactionJpaEntity toEntity(LedgerTransaction domain) {
        if (domain == null) return null;

        List<LedgerEntryJpaEntity> entryEntities = domain.getEntries().stream()
                .map(entryMapper::toEntity)
                .toList();

        return new LedgerTransactionJpaEntity(
                domain.getId(),
                domain.getTransactionReference().transactionId(),
                domain.getTransactionReference().sourceSystem(),
                domain.getPostedAt(),
                entryEntities
        );
    }

    // toDomain not strictly required yet for ledger_transactions as it is append-only, 
    // but typically we'd add it here if we need to load transactions by ID.
}
