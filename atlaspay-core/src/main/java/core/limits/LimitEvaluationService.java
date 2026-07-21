package core.limits;

import core.accounts.valueobjects.AccountId;
import core.identity.valueobject.KycTier;
import core.ledger.valueobjects.Money;
import core.shared.Result;

import java.util.List;

public interface LimitEvaluationService {
    Result<Void, List<String>> evaluate(AccountId accountId, KycTier tier, Money proposedAmount); // composes LimitPolicy.evaluateSingleTransaction + RollingWindowLimitChecker.checkAgainstWindow into one decision
}
