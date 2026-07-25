package persistence.accounts;

import core.accounts.entities.Account;
import core.accounts.repository.AccountRepository;
import core.accounts.valueobjects.AccountId;
import core.accounts.valueobjects.AccountNumber;
import core.identity.valueobject.CompanyId;
import core.shared.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public class JpaAccountRepository implements AccountRepository {

    private final SpringDataAccountRepository repository;
    private final AccountMapper mapper;

    public JpaAccountRepository(SpringDataAccountRepository repository, AccountMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Account> findByCompanyId(CompanyId companyId) {
        return repository.findByCompanyId(companyId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Account> findByAccountNumber(AccountNumber number) {
        return repository.findByAccountNumber(number)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return repository.findById(accountId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Account save(Account aggregate) {
        AccountJpaEntity entity = mapper.toJpaEntity(aggregate);
        AccountJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public PageResult<Account> findAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<AccountJpaEntity> page = repository.findAll(pageable);
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
