package persistence.identity.company;

import core.identity.Company;
import core.identity.CompanyRepository;
import core.identity.valueobject.CompanyId;
import core.identity.valueobject.RegistrationNumber;
import core.shared.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public final class JpaCompanyRepository implements CompanyRepository {

    private final SpringDataCompanyRepository repository;
    private final CompanyMapper mapper;

    public JpaCompanyRepository(SpringDataCompanyRepository repository, CompanyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    public Optional<Company> findByRegistrationNumber(RegistrationNumber number) {
        return repository.findByRegistrationNumber(number.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Company> findById(CompanyId companyId) {
        return repository.findById(companyId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Company save(Company aggregate) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(aggregate)));
    }

    @Override
    public PageResult<Company> findAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<CompanyJpaEntity> page = repository.findAll(pageable);
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
