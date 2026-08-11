package com.atlaspay.identity.domain.model;

/**
 * The five ordered compliance steps a Merchant must complete to unlock LIVE mode.
 * Steps must be completed in the declared order:
 * PROFILE → CONTACT → OWNER → ACCOUNT → SERVICE_AGREEMENT
 */
public enum ComplianceStep {
    PROFILE,
    CONTACT,
    OWNER,
    ACCOUNT,
    SERVICE_AGREEMENT;

    /**
     * Returns the next step in sequence, or null if this is the last step.
     */
    public ComplianceStep next() {
        ComplianceStep[] steps = values();
        int nextIndex = this.ordinal() + 1;
        return nextIndex < steps.length ? steps[nextIndex] : null;
    }
}
