package core.accounts;

import core.accounts.valueobjects.AccountId;
import core.accounts.valueobjects.Balance;

import java.time.Instant;

public interface BalanceCalculator {
    Balance currentBalance(AccountId accountId);

    Balance balanceAsOf(AccountId accountId, Instant pointInTime);
}
