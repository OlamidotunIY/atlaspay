package persistence.identity;

import core.identity.entities.KycCase;
import core.identity.repository.KycCaseRepository;
import core.identity.valueobject.CustomerId;
import core.identity.valueobject.KycCaseId;
import core.shared.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class JpaKycCaseRepository implements KycCaseRepository {

    private final SpringDataKycCaseRepository repository;
    private final KycCaseMapper mapper;

    public JpaKycCaseRepository(SpringDataKycCaseRepository repository, KycCaseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<KycCase> findActiveByCustomerId(CustomerId customerId) {
        return repository.findActiveByCustomerId(customerId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<KycCase> findById(KycCaseId kycCaseId) {
        return repository.findById(kycCaseId.value())
                .map(mapper::toDomain);
    }

    @Override
    public KycCase save(KycCase aggregate) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(aggregate)));
    }

    @Override
    public PageResult<KycCase> findAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<KycCaseJpaEntity> page = repository.findAll(pageable);
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
