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

    private final CustomerId customerId;      // the Customer this case's outcome will ultimately be applied to
    private final Set<String> requiredChecks; // resolved once, per country/targetTier, via KycRequirementPolicy — never hardcoded here
    private final Set<String> completedChecks; // check names recorded so far (passed only), used to determine readiness
    private KycEvaluationSagaState state;

    KycEvaluationSaga(KycCaseId kycCaseId, CustomerId customerId, Set<String> requiredChecks) {
        super(kycCaseId);
        this.customerId = Objects.requireNonNull(customerId);
        this.requiredChecks = Set.copyOf(Objects.requireNonNull(requiredChecks, "requiredChecks must not be null"));
        this.completedChecks = new HashSet<>();
        this.state = KycEvaluationSagaState.AWAITING_CHECKS;
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

        if (!requiredChecks.contains(event.checkName())) {
            throw new IllegalArgumentException("Unrecognized check for this case: " + event.checkName());
        }

        if (event.passed()) {
            completedChecks.add(event.checkName());
        }

        boolean ready = completedChecks.containsAll(requiredChecks);

        state = ready ? KycEvaluationSagaState.EVALUATING : KycEvaluationSagaState.AWAITING_CHECKS;
        // Transition alone signals readiness; the orchestrator watches for
        // EVALUATING and then calls KycRuleEngine.evaluate(...) itself,
        // since this aggregate holds no service/repository references.
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
