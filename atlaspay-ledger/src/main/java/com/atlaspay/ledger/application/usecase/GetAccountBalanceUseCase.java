package com.atlaspay.ledger.application.usecase;

import com.atlaspay.ledger.application.query.GetAccountBalanceQuery;
import com.atlaspay.ledger.domain.model.BalanceSnapshot;
import com.atlaspay.ledger.domain.model.EntryType;
import com.atlaspay.ledger.domain.model.LedgerEntry;
import com.atlaspay.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlaspay.ledger.domain.repository.LedgerEntryRepository;
import com.atlaspay.shared.money.CurrencyCode;
import com.atlaspay.shared.money.Money;
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

    public GetAccountBalanceUseCase(BalanceSnapshotRepository balanceSnapshotRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Money execute(GetAccountBalanceQuery query) {
        // Fetch O(1) Snapshot
        Optional<BalanceSnapshot> snapshotOpt = balanceSnapshotRepository.findLatestByAccountId(query.accountId());

        Money baseBalance;
        Long lastEntryId;
        CurrencyCode accountCurrency;

        if (snapshotOpt.isPresent()) {
            BalanceSnapshot snapshot = snapshotOpt.get();
            baseBalance = snapshot.getBalance();
            lastEntryId = snapshot.getLastLedgerEntryId();
            accountCurrency = snapshot.getBalance().currency();
        } else {
            // No snapshot implies base balance is zero. We assume default currency is NGN unless entries dictate otherwise.
            // A more robust system would fetch account details, but we will infer from entries or return zero NGN.
            accountCurrency = CurrencyCode.NGN; 
            baseBalance = Money.zero(accountCurrency);
            lastEntryId = 0L;
        }

        // Fetch O(k) Delta Entries since snapshot
        List<LedgerEntry> recentEntries = ledgerEntryRepository.findByAccountIdAndIdGreaterThan(query.accountId(), lastEntryId);

        if (recentEntries.isEmpty()) {
            return baseBalance;
        }

        // If it was assumed zero, infer the correct currency from the first entry to prevent currency mismatch
        if (snapshotOpt.isEmpty()) {
            accountCurrency = recentEntries.get(0).getAmount().currency();
            baseBalance = Money.zero(accountCurrency);
        }

        // Calculate Net Delta utilizing Java Streams over List (Liabilities model: Credit adds, Debit subtracts)
        Money finalBaseBalance = baseBalance;
        Money netDelta = recentEntries.stream()
                .map(entry -> entry.getType() == EntryType.CREDIT ? entry.getAmount() : entry.getAmount().negate())
                .reduce(Money.zero(accountCurrency), Money::add);

        return finalBaseBalance.add(netDelta);
    }
}
