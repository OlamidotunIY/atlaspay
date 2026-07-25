package core.accounts.repository;

import core.accounts.entities.Account;
import core.accounts.entities.AccountClosurePermit;
import core.shared.Result;

public interface AccountClosureService {
    Result<AccountClosurePermit, String> canClose(Account account);
}
