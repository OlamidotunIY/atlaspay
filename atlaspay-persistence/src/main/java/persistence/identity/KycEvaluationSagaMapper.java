package persistence.identity;

import core.identity.entities.KycEvaluationSaga;
import core.identity.valueobject.CustomerId;
import core.identity.valueobject.KycCaseId;

public final class KycEvaluationSagaMapper {
    public KycEvaluationSagaJpaEntity toJpaEntity(KycEvaluationSaga domain) {
        return new KycEvaluationSagaJpaEntity(
                domain.id().value(),
                domain.getCustomerId().value(),
                domain.getRequiredChecks(),
                domain.getCompletedChecks(),
                domain.getState()
        );
    }

    public KycEvaluationSaga toDomain(KycEvaluationSagaJpaEntity entity) {
        return KycEvaluationSaga.rehydrate(
                new KycCaseId(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                entity.getRequiredChecks(),
                entity.getCompletedChecks(),
                entity.getState()
        );
    }
}
