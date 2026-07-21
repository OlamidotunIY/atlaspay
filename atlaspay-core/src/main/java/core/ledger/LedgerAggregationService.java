package core.ledger;

import core.accounts.valueobjects.AccountId;
import core.ledger.valueobjects.Money;

import java.time.Instant;

public interface LedgerAggregationService {
    Money sumLines(AccountId accountId, Instant from, Instant to); // net of all DEBIT/CREDIT lines for the account within [from, to)
}
