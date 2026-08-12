package com.atlaspay.identity.domain.model;

/**
 * The merchant's compliance verification status.
 * Transitions are strictly one-way and ordered.
 *
 * NOT_STARTED → IN_PROGRESS → SUBMITTED → UNDER_REVIEW → APPROVED
 *                                                       ↘ REJECTED
 *
 * APPROVED is the terminal success state that unlocks LIVE mode API keys.
 */
public enum ComplianceStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED
}
