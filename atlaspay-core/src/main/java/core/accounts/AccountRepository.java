package core.accounts;

import core.accounts.valueobjects.AccountId;
import core.accounts.valueobjects.AccountNumber;
import core.identity.valueobject.CompanyId;
import core.shared.Repository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends Repository<Account, AccountId> {
    List<Account> findByCompanyId(CompanyId companyId);

    Optional<Account> findByAccountNumber(AccountNumber number);
}
