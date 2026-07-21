package core.identity;

import core.identity.valueobject.KycTier;

import java.util.Set;

/**
 * Resolves which named checks (e.g. "PERSONAL_DETAILS", "BVN", "NIN",
 * "COMPANY_REGISTRATION_DOCS") must all pass before a {@link KycCase} targeting
 * a given {@link KycTier} is ready for {@link KycRuleEngine} evaluation.
 *
 * Requirements vary by country (regulatory documents differ per jurisdiction),
 * so this is a port: concrete rule sets are supplied by an adapter (config table,
 * feature-flag service, etc.) in atlaspay-persistence, never hardcoded in core.
 * Dependency Inversion — the domain declares what it needs, not how it's sourced.
 */
public interface KycRequirementPolicy {
    Set<String> requiredChecks(String countryCode, KycTier targetTier); // e.g. NG + TIER_2 -> {BVN, NIN}
}
