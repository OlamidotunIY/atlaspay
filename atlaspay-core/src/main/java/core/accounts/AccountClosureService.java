package core.accounts;

import core.shared.Result;

public interface AccountClosureService {
    Result<AccountClosurePermit, String> canClose(Account account);
}
