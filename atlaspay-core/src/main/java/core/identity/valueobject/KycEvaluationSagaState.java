package core.identity.valueobject;

public enum KycEvaluationSagaState {
    AWAITING_CHECKS,
    EVALUATING,
    TIER_APPROVED,
    REJECTED,
    DECIDED
}
