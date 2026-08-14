package com.atlaspay.ledger.application.usecase;

import com.atlaspay.ledger.application.query.GetAccountBalanceQuery;
import com.atlaspay.ledger.domain.model.BalanceSnapshot;
import com.atlaspay.ledger.domain.model.EntryType;
import com.atlaspay.ledger.domain.model.LedgerEntry;
import com.atlaspay.ledger.domain.model.TransactionReference;
import com.atlaspay.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlaspay.ledger.domain.repository.LedgerEntryRepository;
import com.atlaspay.shared.money.CurrencyCode;
import com.atlaspay.shared.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GetAccountBalanceUseCaseTest {

    private BalanceSnapshotRepository snapshotRepository;
    private LedgerEntryRepository entryRepository;
    private GetAccountBalanceUseCase useCase;

    @BeforeEach
    void setUp() {
        snapshotRepository = mock(BalanceSnapshotRepository.class);
        entryRepository = mock(LedgerEntryRepository.class);
        useCase = new GetAccountBalanceUseCase(snapshotRepository, entryRepository);
    }

    @Test
    void shouldCalculateBalanceFromScratchWhenNoSnapshot() {
        GetAccountBalanceQuery query = new GetAccountBalanceQuery(10L);

        when(snapshotRepository.findLatestByAccountId(10L)).thenReturn(Optional.empty());

        TransactionReference ref = new TransactionReference("TX-1", "SYS");
        LedgerEntry entry1 = new LedgerEntry(1L, 10L, Money.of("500", CurrencyCode.NGN), EntryType.CREDIT, ref, "Initial", ZonedDateTime.now());
        LedgerEntry entry2 = new LedgerEntry(2L, 10L, Money.of("200", CurrencyCode.NGN), EntryType.DEBIT, ref, "Withdraw", ZonedDateTime.now());

        when(entryRepository.findByAccountIdAndIdGreaterThan(10L, 0L)).thenReturn(List.of(entry1, entry2));

        Money result = useCase.execute(query);

        assertEquals(Money.of("300.0000", CurrencyCode.NGN), result);
    }

    @Test
    void shouldCalculateBalanceFromSnapshotPlusDelta() {
        GetAccountBalanceQuery query = new GetAccountBalanceQuery(10L);

        BalanceSnapshot snapshot = new BalanceSnapshot(1L, 10L, Money.of("1000", CurrencyCode.NGN), 100L, ZonedDateTime.now());
        when(snapshotRepository.findLatestByAccountId(10L)).thenReturn(Optional.of(snapshot));

        TransactionReference ref = new TransactionReference("TX-2", "SYS");
        LedgerEntry entry1 = new LedgerEntry(101L, 10L, Money.of("500", CurrencyCode.NGN), EntryType.CREDIT, ref, "Deposit", ZonedDateTime.now());

        when(entryRepository.findByAccountIdAndIdGreaterThan(10L, 100L)).thenReturn(List.of(entry1));

        Money result = useCase.execute(query);

        assertEquals(Money.of("1500.0000", CurrencyCode.NGN), result);
    }

    @Test
    void shouldReturnSnapshotBalanceWhenNoRecentEntries() {
        GetAccountBalanceQuery query = new GetAccountBalanceQuery(10L);

        BalanceSnapshot snapshot = new BalanceSnapshot(1L, 10L, Money.of("1000", CurrencyCode.NGN), 100L, ZonedDateTime.now());
        when(snapshotRepository.findLatestByAccountId(10L)).thenReturn(Optional.of(snapshot));

        when(entryRepository.findByAccountIdAndIdGreaterThan(10L, 100L)).thenReturn(List.of());

        Money result = useCase.execute(query);

        assertEquals(Money.of("1000.0000", CurrencyCode.NGN), result);
    }

    @Test
    void shouldReturnZeroWhenNoSnapshotAndNoEntries() {
        GetAccountBalanceQuery query = new GetAccountBalanceQuery(10L);

        when(snapshotRepository.findLatestByAccountId(10L)).thenReturn(Optional.empty());
        when(entryRepository.findByAccountIdAndIdGreaterThan(10L, 0L)).thenReturn(List.of());

        Money result = useCase.execute(query);

        assertEquals(Money.zero(CurrencyCode.NGN), result);
    }
}
