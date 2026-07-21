package core.cards;

import core.cards.events.DisputeFiled;
import core.cards.valueobjects.ChargebackSagaState;
import core.cards.valueobjects.DisputeId;
import core.shared.AggregateRoot;

import java.util.Objects;

public final class ChargebackSaga extends AggregateRoot<DisputeId> {
    private ChargebackSagaState state;

    public ChargebackSaga(DisputeId id) {
        super(id);
    }

    public void onDisputeFiled(DisputeFiled event) {
        Objects.requireNonNull(event, "event cannot be null");
        if (state != null) {
            throw new IllegalStateException("Dispute already filed");
        }
        state = ChargebackSagaState.FILED;
    }

    public void onReviewStarted() {
        if (state != ChargebackSagaState.FILED) {
            throw new IllegalStateException("Cannot start review, dispute not filed");
        }
        state = ChargebackSagaState.UNDER_REVIEW;
    }

    public void onProvisionalCreditRequested() {
        if (state != ChargebackSagaState.UNDER_REVIEW) {
            throw new IllegalStateException("Cannot request provisional credit, dispute not under review");
        }
        state = ChargebackSagaState.PROVISIONAL_CREDIT_ISSUED;
    }

    public void onMerchantLiabilityConfirmed() {
        if (state != ChargebackSagaState.PROVISIONAL_CREDIT_ISSUED) {
            throw new IllegalStateException("Cannot confirm merchant liability, provisional credit not issued");
        }
        state = ChargebackSagaState.RESOLVED_CUSTOMER;
    }

    public void onCustomerClaimRejected() {
        if (state != ChargebackSagaState.PROVISIONAL_CREDIT_ISSUED) {
            throw new IllegalStateException("Cannot reject customer claim, provisional credit not issued");
        }
        state = ChargebackSagaState.RESOLVED_MERCHANT;
    }

    public void onProvisionalCreditReversed() {
        if (state != ChargebackSagaState.RESOLVED_MERCHANT) {
            throw new IllegalStateException("Cannot reverse provisional credit, dispute not resolved in favor of merchant");
        }
        state = ChargebackSagaState.REVERSAL_OF_PROVISIONAL_CREDIT;
    }

    public void onEvidenceInsufficient() {
        if (state != ChargebackSagaState.UNDER_REVIEW) {
            throw new IllegalStateException("Cannot mark evidence as insufficient, dispute not under review");
        }
        state = ChargebackSagaState.RESOLVED_MERCHANT;
    }
}
