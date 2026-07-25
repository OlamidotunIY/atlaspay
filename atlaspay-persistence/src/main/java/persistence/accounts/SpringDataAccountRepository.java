package persistence.accounts;

import core.accounts.valueobjects.AccountNumber;
import core.identity.valueobject.CompanyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
    List<AccountJpaEntity> findByCompanyId(CompanyId companyId);

    Optional<AccountJpaEntity> findByAccountNumber(AccountNumber number);
}
