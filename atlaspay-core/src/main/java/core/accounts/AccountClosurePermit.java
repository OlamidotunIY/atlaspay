package core.accounts;

import core.accounts.valueobjects.AccountId;

import java.time.Instant;
import java.util.Objects;

public class AccountClosurePermit {

    private final AccountId accountId;   // the specific account this permit was evaluated for
    private final Instant issuedAt;        // when the zero-balance check was performed

    // package-private: only AccountClosureService's implementation (inside core.accounts)
    // can legitimately construct one — see DESIGN.md rationale.
    AccountClosurePermit(AccountId accountId, Instant issuedAt) {
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt must not be null");
    }

    AccountId accountId() {
        return accountId;
    }

    Instant issuedAt() {
        return issuedAt;
    }
}
