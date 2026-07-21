package core.limits;

import core.identity.valueobject.KycTier;
import core.limits.valueobjects.LimitPolicyId;
import core.shared.Repository;

import java.util.Optional;

public interface LimitPolicyRepository extends Repository<LimitPolicy, LimitPolicyId> {
    Optional<LimitPolicy> findByTier(KycTier tier);
}
