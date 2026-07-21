package core.ledger.valueobjects;

import core.accounts.valueobjects.AccountId;

import java.util.Objects;

public record LedgerLine(AccountId accountId, Money amount, EntryDirection direction) {
    public LedgerLine {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(direction, "direction must not be null");
    }
}
