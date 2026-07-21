package core.transfers;

import core.ledger.valueobjects.JournalEntryId;
import core.ledger.valueobjects.Money;
import core.shared.AggregateRoot;
import core.transfers.events.TransferFailed;
import core.transfers.events.TransferPosted;
import core.transfers.valueobjects.TransferId;
import core.transfers.valueobjects.TransferStatus;

import java.time.Instant;
import java.util.UUID;

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

    public void markPosted(JournalEntryId entryId) {
        if (status != TransferStatus.INITIATED) {
            throw new IllegalStateException("Transfer can only be marked as posted if it is in the INITIATED state");
        }
        this.status = TransferStatus.POSTED;
        register(new TransferPosted(UUID.randomUUID(), Instant.now(), this.id(), entryId));
    }

    public void markFailed(String reason) {
        if (status != TransferStatus.INITIATED) {
            throw new IllegalStateException("Transfer can only be marked as failed if it is in the INITIATED state");
        }
        this.status = TransferStatus.FAILED;
        register(new TransferFailed(UUID.randomUUID(), Instant.now(), this.id(), reason));
    }
}
