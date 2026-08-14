package com.atlaspay.ledger.domain.model;

import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.ledger.domain.exception.LedgerErrorCode;
import com.atlaspay.shared.money.Money;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LedgerTransaction extends AggregateRoot<Long> {
    private final Long id;
    @Getter
    private final TransactionReference transactionReference;
    private final List<LedgerEntry> entries;
    private final ZonedDateTime postedAt;

    public LedgerTransaction(Long id, TransactionReference transactionReference, List<LedgerEntry> entries, ZonedDateTime postedAt) {
        if (transactionReference == null) {
            throw new IllegalArgumentException("Transaction reference cannot be null");
        }
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("Ledger transaction must have at least one entry");
        }

        validateEntriesBalance(entries);

        this.id = id;
        this.transactionReference = transactionReference;
        this.entries = new ArrayList<>(entries);
        this.postedAt = postedAt != null ? postedAt : ZonedDateTime.now();
    }

    private void validateEntriesBalance(List<LedgerEntry> entriesToValidate) {
        if (entriesToValidate.size() < 2) {
            throw new BusinessRuleException(LedgerErrorCode.INVALID_TRANSACTION_STATE, "A ledger transaction must have at least two entries");
        }

        Money totalDebits = null;
        Money totalCredits = null;

        for (LedgerEntry entry : entriesToValidate) {
            if (entry.getType() == EntryType.DEBIT) {
                if (totalDebits == null) {
                    totalDebits = entry.getAmount();
                } else {
                    totalDebits = totalDebits.add(entry.getAmount());
                }
            } else if (entry.getType() == EntryType.CREDIT) {
                if (totalCredits == null) {
                    totalCredits = entry.getAmount();
                } else {
                    totalCredits = totalCredits.add(entry.getAmount());
                }
            }
        }

        if (totalDebits == null || totalCredits == null) {
            throw new BusinessRuleException(LedgerErrorCode.INVALID_TRANSACTION_STATE, "A ledger transaction must have both debit and credit entries");
        }

        if (!totalDebits.equals(totalCredits)) {
            throw new BusinessRuleException(LedgerErrorCode.UNBALANCED_TRANSACTION, "Total debits must equal total credits");
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    public List<LedgerEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public ZonedDateTime getPostedAt() {
        return postedAt;
    }
}
