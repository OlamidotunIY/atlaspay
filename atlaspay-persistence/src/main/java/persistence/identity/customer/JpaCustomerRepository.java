package persistence.identity.customer;

import core.identity.Customer;
import core.identity.CustomerRepository;
import core.identity.valueobject.CompanyId;
import core.identity.valueobject.CustomerId;
import core.shared.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public final class JpaCustomerRepository implements CustomerRepository {

    private final SpringDataCustomerRepository repository;
    private final CustomerMapper mapper;

    public JpaCustomerRepository(SpringDataCustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Customer> findByCompanyId(CompanyId companyId) {
        return repository.findByCompanyId(companyId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return repository.findById(customerId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Customer save(Customer aggregate) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(aggregate)));
    }

    @Override
    public PageResult<Customer> findAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<CustomerJpaEntity> page = repository.findAll(pageable);
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
