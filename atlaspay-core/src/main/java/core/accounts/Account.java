package core.accounts;

import core.accounts.events.AccountClosed;
import core.accounts.events.AccountFrozen;
import core.accounts.events.AccountOpened;
import core.accounts.valueobjects.AccountId;
import core.accounts.valueobjects.AccountNumber;
import core.accounts.valueobjects.AccountStatus;
import core.accounts.valueobjects.AccountType;
import core.identity.valueobject.CustomerId;
import core.shared.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Account extends AggregateRoot<AccountId> {

    private final AccountNumber accountNumber;    // externally visible VO
    private final CustomerId ownerId;               // owning customer reference by ID
    private AccountType type;                       // WALLET, SETTLEMENT, etc.
    private AccountStatus status;                    // ACTIVE, FROZEN, CLOSED

    public Account(AccountId accountId, AccountNumber accountNumber, CustomerId ownerId, AccountType type) {
        super(accountId);
        this.accountNumber = accountNumber;
        this.ownerId = ownerId;
        this.type = type;
        this.status = AccountStatus.ACTIVE;

        register(new AccountOpened(UUID.randomUUID(), Instant.now(), accountId, ownerId));
    }

    public void freeze(String reason) {
        Objects.requireNonNull(reason, "reason must not be null");

        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Only an ACTIVE account can be frozen, current status: " + this.status);
        }

        this.status = AccountStatus.FROZEN;
        register(new AccountFrozen(UUID.randomUUID(), Instant.now(), this.id(), reason));
    }

    public void close(AccountClosurePermit permit) {
        Objects.requireNonNull(permit, "permit must not be null");

        if (!permit.accountId().equals(this.id())) {
            throw new IllegalArgumentException("Permit was not issued for this account");
        }
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account is already closed");
        }

        this.status = AccountStatus.CLOSED;
        register(new AccountClosed(UUID.randomUUID(), Instant.now(), this.id()));
    }

    public AccountStatus status() {
        return status;
    }
}
