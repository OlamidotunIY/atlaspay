package core.identity;

import core.identity.valueobject.CustomerId;
import core.identity.valueobject.KycCaseId;
import core.shared.Repository;

import java.util.Optional;

public interface KycCaseRepository extends Repository<KycCase, KycCaseId> {
    Optional<KycCase> findActiveByCustomerId(CustomerId customerId); // find in-flight case
}
