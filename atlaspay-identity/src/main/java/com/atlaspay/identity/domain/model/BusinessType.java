package com.atlaspay.identity.domain.model;

/**
 * Indicates whether a Merchant is an informal/unregistered business (STARTER)
 * or a CAC-registered business (REGISTERED).
 * REGISTERED merchants must supply an rcNumber before completing the OWNER compliance step.
 */
public enum BusinessType {
    STARTER,
    REGISTERED
}
