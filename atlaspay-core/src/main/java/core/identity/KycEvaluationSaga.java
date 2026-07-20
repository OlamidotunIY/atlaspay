package core.identity;

import core.identity.events.KycCaseOpened;
import core.identity.events.KycCheckResultRecorded;
import core.identity.valueobject.CustomerId;
import core.identity.valueobject.KycCaseId;
import core.identity.valueobject.KycEvaluationSagaState;
import core.identity.valueobject.KycTier;
import core.shared.AggregateRoot;
import core.shared.Result;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class KycEvaluationSaga extends AggregateRoot<KycCaseId> {
    private static final Set<String> REQUIRED_CHECKS = Set.of("IDENTITY", "SANCTIONS", "ADDRESS");

    private final CustomerId customerId;      // the Customer this case's outcome will ultimately be applied to
    private KycEvaluationSagaState state;
    private final Set<String> completedChecks;

    KycEvaluationSaga(KycCaseId kycCaseId, CustomerId customerId) {
        super(kycCaseId);
        this.customerId = customerId;
        this.state = KycEvaluationSagaState.AWAITING_CHECKS;
        this.completedChecks = new HashSet<>();
    }


    public void onKycCaseOpened(KycCaseOpened event) {
        Objects.requireNonNull(event);
        if (state != KycEvaluationSagaState.AWAITING_CHECKS) {
            throw new IllegalStateException("Cannot open case in state: " + state);
        }
        // saga already starts in AWAITING_CHECKS via the constructor; this is
        // the explicit event-driven entry point confirming that transition.
        state = KycEvaluationSagaState.AWAITING_CHECKS;
    }

    public void onKycCheckResultRecorded(KycCheckResultRecorded event) {
        Objects.requireNonNull(event);

        if (state != KycEvaluationSagaState.AWAITING_CHECKS) {
            throw new IllegalStateException("Unexpected check result in state: " + state);
        }

        if (event.passed()) {
            completedChecks.add(event.checkName());
        }

        boolean ready = completedChecks.containsAll(REQUIRED_CHECKS);

        state = ready ? KycEvaluationSagaState.EVALUATING : KycEvaluationSagaState.AWAITING_CHECKS;
    }

    public void onEvaluationCompleted(Result<KycTier, List<String>> result) {
        Objects.requireNonNull(result);

        if (state != KycEvaluationSagaState.EVALUATING) {
            throw new IllegalStateException("Cannot complete evaluation in state: " + state);
        }

        state = result.isOk() ? KycEvaluationSagaState.TIER_APPROVED : KycEvaluationSagaState.REJECTED;
        // Orchestrator reacts to this state: on TIER_APPROVED it loads
        // Customer via customerId, calls recordKycDecision(APPROVED) then
        // applyKycTier(tier); on REJECTED it calls recordKycDecision(REJECTED).
        // After either, orchestrator transitions this saga to DECIDED.
    }

    public KycEvaluationSagaState state() {
        return state;
    }

    public CustomerId customerId() {
        return customerId;
    }
}
