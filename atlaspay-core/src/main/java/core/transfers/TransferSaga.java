package core.transfers;

import core.accounts.valueobjects.AccountId;
import core.ledger.valueobjects.JournalEntryId;
import core.limits.events.LimitBreached;
import core.limits.valueobjects.LimitPolicyId;
import core.shared.AggregateRoot;
import core.shared.Result;
import core.transfers.events.TransferInitiated;
import core.transfers.valueobjects.ExternalPartyReference;
import core.transfers.valueobjects.TransferId;
import core.transfers.valueobjects.TransferSagaState;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TransferSaga extends AggregateRoot<TransferId> {

    private TransferSagaState state;
    private AccountId accountId;
    private JournalEntryId entryId;

    protected TransferSaga(TransferId id) {
        super(id);
    }

    public void onTransferInitiated(TransferInitiated event) {
        Objects.requireNonNull(event);
        if (state != null) {
            throw new IllegalStateException("Transfer already initiated for this saga: " + state);
        }
        state = TransferSagaState.INITIATED;
        accountId = event.accountId();
    }

    public void onLimitsCheckResult(Result<Void, List<String>> result, LimitPolicyId policyId) {
        Objects.requireNonNull(result, "result must not be null");
        Objects.requireNonNull(policyId, "policyId must not be null");
        if (this.state != TransferSagaState.INITIATED) {
            throw new IllegalStateException("Cannot process limits check result in state: " + this.state);
        }

        if (result.isOk()) {
            this.state = TransferSagaState.VALIDATED;
        } else {
            this.state = TransferSagaState.FAILED;
            register(new LimitBreached(UUID.randomUUID(), Instant.now(), accountId, policyId));
        }
    }

    public void onLedgerPostingSucceeded(JournalEntryId entryId) {
        Objects.requireNonNull(entryId, "entryId must not be null");
        if (this.state != TransferSagaState.VALIDATED) {
            throw new IllegalStateException("Cannot process ledger posting result in state: " + this.state);
        }
        this.state = TransferSagaState.POSTED;
        this.entryId = entryId;

    }

    public void onLedgerPostingFailed(String reason) {
        Objects.requireNonNull(reason, "reason must not be null");
        if (this.state != TransferSagaState.VALIDATED) {
            throw new IllegalStateException("Cannot process ledger posting result in state: " + this.state);
        }
        this.state = TransferSagaState.FAILED;
    }

    public void onExternalConfirmationReceived(ExternalPartyReference ref) {
        Objects.requireNonNull(ref, "ref must not be null");
        if (state != TransferSagaState.POSTED) {
            throw new IllegalStateException("Cannot process external confirmation in state: " + state);
        }
        state = TransferSagaState.COMPLETED;
    }

    public void onReversalRequested(String reason) {
        Objects.requireNonNull(reason, "reason must not be null");
        if (state != TransferSagaState.POSTED) {
            throw new IllegalStateException("Cannot process reversal request in state: " + state);
        }
        Objects.requireNonNull(entryId, "Cannot reverse a transfer with no posted journal entry");
        state = TransferSagaState.REVERSED;
    }

    public JournalEntryId entryId() {
        return entryId;
    }
}
