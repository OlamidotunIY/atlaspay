package com.atlaspay.ledger.application.usecase;

import com.atlaspay.ledger.application.query.GetAccountBalanceQuery;
import com.atlaspay.ledger.domain.model.BalanceSnapshot;
import com.atlaspay.ledger.domain.model.EntryType;
import com.atlaspay.ledger.domain.model.LedgerEntry;
import com.atlaspay.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlaspay.ledger.domain.repository.LedgerEntryRepository;
import com.atlaspay.shared.money.CurrencyCode;
import com.atlaspay.shared.money.Money;
import com.atlaspay.shared.port.out.AccountQueryPort;
import com.atlaspay.shared.port.out.AccountDetailsDto;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.ledger.domain.exception.LedgerErrorCode;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GetAccountBalanceUseCase extends BaseUseCase<GetAccountBalanceQuery, Money> {

    private final BalanceSnapshotRepository balanceSnapshotRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountQueryPort accountQueryPort;

    public GetAccountBalanceUseCase(BalanceSnapshotRepository balanceSnapshotRepository, LedgerEntryRepository ledgerEntryRepository, AccountQueryPort accountQueryPort) {
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountQueryPort = accountQueryPort;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Money execute(GetAccountBalanceQuery query) {
        // Fetch O(1) Snapshot
        Optional<BalanceSnapshot> snapshotOpt = balanceSnapshotRepository.findLatestByAccountId(query.accountId());

        // Fetch Account Details and Validate Ownership
        AccountDetailsDto account = accountQueryPort.findAccountDetails(query.accountId())
                .orElseThrow(() -> new NotFoundException(LedgerErrorCode.ACCOUNT_NOT_FOUND, "Account does not exist"));

        if (!account.integration().equals(query.integration())) {
            throw new BusinessRuleException(LedgerErrorCode.UNAUTHORIZED_ACCESS, "Account does not belong to integration");
        }

        if (snapshotOpt.isPresent()) {
            BalanceSnapshot snapshot = snapshotOpt.get();
            return snapshot.getBalance();
        } else {
            return Money.zero(account.currency());
        }
    }
}
