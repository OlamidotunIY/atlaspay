package core.transfers;

import core.accounts.valueobjects.AccountId;
import core.shared.Repository;
import core.transfers.valueobjects.TransferId;

import java.util.List;

public interface TransferRepository extends Repository<Transfer, TransferId> {
    List<Transfer> findByAccountId(AccountId accountId); // history for either side of a transfer
}

