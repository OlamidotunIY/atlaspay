package core.transfers;

import core.accounts.valueobjects.AccountId;
import core.ledger.valueobjects.JournalEntryId;
import core.ledger.valueobjects.Money;
import core.transfers.valueobjects.TransferId;

public final class InternalTransfer extends Transfer {

    private final AccountId sourceAccountId;
    private final AccountId destinationAccountId;

    public InternalTransfer(TransferId id, Money amount, AccountId sourceAccountId, AccountId destinationAccountId) {
        super(id, amount);
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
    }

    @Override
    public void markPosted(JournalEntryId entryId) {

    }

    @Override
    public void markFailed(String reason) {

    }
}
