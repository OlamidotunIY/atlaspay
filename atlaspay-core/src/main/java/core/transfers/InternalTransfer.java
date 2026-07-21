package core.transfers;

import core.accounts.valueobjects.AccountId;
import core.ledger.valueobjects.Money;
import core.transfers.valueobjects.TransferId;

public final class InternalTransfer extends Transfer {

    private final AccountId sourceAccountId;
    private final AccountId destinationAccountId;

    public InternalTransfer(TransferId id, Money amount, AccountId sourceAccountId, AccountId destinationAccountId) {
        super(id, amount);

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
    }

    public AccountId getSourceAccountId() {
        return sourceAccountId;
    }

    public AccountId getDestinationAccountId() {
        return destinationAccountId;
    }
}
