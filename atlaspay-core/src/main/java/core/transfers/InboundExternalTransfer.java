package core.transfers;

import core.accounts.valueobjects.AccountId;
import core.ledger.valueobjects.Money;
import core.transfers.valueobjects.ExternalPartyReference;
import core.transfers.valueobjects.TransferId;

public final class InboundExternalTransfer extends Transfer {

    private final AccountId destinationAccountId;
    private final ExternalPartyReference originatingParty;

    public InboundExternalTransfer(TransferId id, Money amount, AccountId destinationAccountId, ExternalPartyReference originatingParty) {
        super(id, amount);
        this.destinationAccountId = destinationAccountId;
        this.originatingParty = originatingParty;
    }

    public ExternalPartyReference originatingParty() {
        return originatingParty;
    }

    public AccountId destinationAccountId() {
        return destinationAccountId;
    }
}
