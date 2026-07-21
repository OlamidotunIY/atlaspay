package core.transfers;

import core.ledger.valueobjects.JournalEntryId;
import core.ledger.valueobjects.Money;
import core.shared.AggregateRoot;
import core.transfers.valueobjects.TransferId;
import core.transfers.valueobjects.TransferStatus;

public sealed abstract class Transfer extends AggregateRoot<TransferId> permits InternalTransfer, InboundExternalTransfer, OutboundExternalTransfer {

    protected final Money amount;
    protected TransferStatus status;

    public Transfer(TransferId id, Money amount) {
        super(id);
        this.amount = amount;
        this.status = TransferStatus.INITIATED;
    }

    public TransferStatus status()                    // accessor
    {
        return status;
    }

    public abstract void markPosted(JournalEntryId entryId); // records successful ledger posting; raises TransferPosted
    public abstract void markFailed(String reason);
}
