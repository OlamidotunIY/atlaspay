package core.identity;

import core.shared.Specification;

public interface KycRule extends Specification<KycCase> {
    String ruleName();
}
