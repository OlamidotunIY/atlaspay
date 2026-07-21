package core.transfers;

import core.accounts.valueobjects.AccountId;
import core.ledger.valueobjects.JournalEntryId;
import core.ledger.valueobjects.Money;
import core.transfers.valueobjects.ExternalPartyReference;
import core.transfers.valueobjects.TransferId;

public final class OutboundExternalTransfer extends Transfer {

    private final AccountId sourceAccountId;
    private final ExternalPartyReference beneficiaryParty;

    public OutboundExternalTransfer(TransferId id, Money amount, AccountId sourceAccountId, ExternalPartyReference beneficiaryParty) {
        super(id, amount);
        this.sourceAccountId = sourceAccountId;
        this.beneficiaryParty = beneficiaryParty;
    }

    @Override
    public void markPosted(JournalEntryId entryId) {

    }

    @Override
    public void markFailed(String reason) {

    }
}
