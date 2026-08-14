package com.atlaspay.ledger.domain.model;

import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.money.Money;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class LedgerEntry extends AggregateRoot<Long> {
    
    private final Long id;
    private final Long accountId;
    private final Money amount;
    private final EntryType type;
    private final TransactionReference transactionReference;
    private final String description;
    private final ZonedDateTime createdAt;

    public LedgerEntry(Long id, Long accountId, Money amount, EntryType type, TransactionReference transactionReference, String description, ZonedDateTime createdAt) {
        if (amount == null || amount.isNegative() || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        if (transactionReference == null) {
            throw new IllegalArgumentException("Transaction reference cannot be null");
        }

        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.transactionReference = transactionReference;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
