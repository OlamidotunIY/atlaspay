package core.transfers;

import core.ledger.valueobjects.JournalEntryId;
import core.shared.AggregateRoot;
import core.shared.Result;
import core.transfers.events.TransferInitiated;
import core.transfers.valueobjects.ExternalPartyReference;
import core.transfers.valueobjects.TransferId;
import core.transfers.valueobjects.TransferSagaState;

import java.util.List;

public class TransferSaga extends AggregateRoot<TransferId> {

    private TransferSagaState state;

    protected TransferSaga(TransferId id) {
        super(id);
    }

    public void onTransferInitiated(TransferInitiated event) {

    }

    public void onLimitsCheckResult(Result<Void, List<String>> result) {
    }

    public void onLedgerPostingSucceeded(JournalEntryId entryId) {
    }

    public void onLedgerPostingFailed(String reason) {
    }

    public void onExternalConfirmationReceived(ExternalPartyReference ref) {
    }

    public void onReversalRequested(String reason) {
    }
}
