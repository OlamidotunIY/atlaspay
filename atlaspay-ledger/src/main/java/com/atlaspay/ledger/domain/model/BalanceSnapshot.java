package com.atlaspay.ledger.domain.model;

import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.money.Money;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class BalanceSnapshot extends AggregateRoot<Long> {
    
    private final Long id;
    private final Long accountId;
    private Money balance;
    private Long lastLedgerEntryId;
    private ZonedDateTime snapshotAt;

    public BalanceSnapshot(Long id, Long accountId, Money balance, Long lastLedgerEntryId, ZonedDateTime snapshotAt) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        if (lastLedgerEntryId == null) {
            throw new IllegalArgumentException("Last ledger entry ID cannot be null");
        }

        this.id = id;
        this.accountId = accountId;
        this.balance = balance;
        this.lastLedgerEntryId = lastLedgerEntryId;
        this.snapshotAt = snapshotAt != null ? snapshotAt : ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }

    public void updateBalance(Money newBalance, Long entryId) {
        if (newBalance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        if (entryId == null) {
            throw new IllegalArgumentException("Entry ID cannot be null");
        }
        this.balance = newBalance;
        this.lastLedgerEntryId = entryId;
        this.snapshotAt = ZonedDateTime.now();
    }
}
