package persistence.identity;

import core.identity.entities.KycCase;
import core.identity.valueobject.CustomerId;
import core.identity.valueobject.KycCaseId;
import core.identity.valueobject.KycCheckResult;

public final class KycCaseMapper {
    public KycCaseJpaEntity toJpaEntity(KycCase kycCase) {
        return new KycCaseJpaEntity(
            kycCase.id().value(),
            kycCase.getCustomerId().value(),
            kycCase.getTargetTier(),
            kycCase.getCheckResults().stream()
                    .map(KycCheckResultEmbeddable::from)
                    .toList(),
            kycCase.getStatus()
        );
    }

    public KycCase toDomain(KycCaseJpaEntity entity) {
        return KycCase.rehydrate(
            new KycCaseId(entity.getId()),
            new CustomerId(entity.getCustomerId()),
            entity.getTargetTier(),
            entity.getCheckResults().stream()
                    .map(checkResult -> new KycCheckResult(
                            checkResult.getCheckName(),
                            checkResult.isPassed(),
                            checkResult.getDetail()
                    ))
                    .toList(),
            entity.getStatus()
        );
    }
}
