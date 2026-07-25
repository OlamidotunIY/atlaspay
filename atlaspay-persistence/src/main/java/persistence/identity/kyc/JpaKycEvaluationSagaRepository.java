package persistence.identity.kyc;

import core.identity.entities.KycEvaluationSaga;
import core.identity.repository.KycEvaluationSagaRepository;
import core.identity.valueobject.KycCaseId;
import core.shared.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class JpaKycEvaluationSagaRepository implements KycEvaluationSagaRepository {

    private final SpringDataKycEvaluationSagaRepository repository;
    private final KycEvaluationSagaMapper mapper;

    public JpaKycEvaluationSagaRepository(SpringDataKycEvaluationSagaRepository repository, KycEvaluationSagaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<KycEvaluationSaga> findById(KycCaseId kycCaseId) {
        return repository.findById(kycCaseId.value())
                .map(mapper::toDomain);
    }

    @Override
    public KycEvaluationSaga save(KycEvaluationSaga aggregate) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(aggregate)));
    }

    @Override
    public PageResult<KycEvaluationSaga> findAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<KycEvaluationSagaJpaEntity> page = repository.findAll(pageable);
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
