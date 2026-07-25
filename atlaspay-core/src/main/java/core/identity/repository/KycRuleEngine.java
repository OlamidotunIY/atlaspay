package core.identity.repository;

import core.identity.entities.Customer;
import core.identity.entities.KycCase;
import core.identity.valueobject.KycTier;
import core.shared.Result;

import java.util.List;

public interface KycRuleEngine {
    Result<KycTier, List<String>> evaluate(Customer customer, KycCase kycCase);
}
