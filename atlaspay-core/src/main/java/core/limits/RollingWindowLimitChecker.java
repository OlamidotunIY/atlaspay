package core.limits;

import core.accounts.valueobjects.AccountId;
import core.ledger.valueobjects.Money;
import core.shared.Result;

public interface RollingWindowLimitChecker {
    Result<Void, String> checkAgainstWindow(AccountId accountId, LimitPolicy policy, Money proposedAmount); // delegates ledger summation to LedgerAggregationService, compares the result to the policy
}
