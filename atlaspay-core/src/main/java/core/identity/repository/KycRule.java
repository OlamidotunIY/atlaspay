package core.identity.repository;

import core.identity.entities.KycCase;
import core.shared.Specification;

public interface KycRule extends Specification<KycCase> {
    String ruleName();
}
