package persistence.accounts;

import core.accounts.entities.Account;
import core.accounts.valueobjects.AccountId;
import core.accounts.valueobjects.AccountNumber;
import core.identity.valueobject.CustomerId;

public final class AccountMapper {
    public AccountJpaEntity toJpaEntity(Account domain) {
        return new AccountJpaEntity(
                domain.id().value(),
                domain.getAccountNumber().value(),
                domain.getOwnerId().value(),
                domain.getType(),
                domain.status()
        );
    }

    public Account toDomain(AccountJpaEntity entity) {
        return new Account(
                new AccountId(entity.getId()),
                new AccountNumber(entity.getAccountNumber()),
                new CustomerId(entity.getOwnerId()),
                entity.getType()
        );
    }
}
