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
import com.atlaspay.shared.port.out.AccountQueryPort;
import com.atlaspay.shared.port.out.AccountDetailsDto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GetAccountBalanceUseCaseTest {

    private BalanceSnapshotRepository snapshotRepository;
    private LedgerEntryRepository entryRepository;
    private AccountQueryPort accountQueryPort;
    private GetAccountBalanceUseCase useCase;

    @BeforeEach
    void setUp() {
        snapshotRepository = mock(BalanceSnapshotRepository.class);
        entryRepository = mock(LedgerEntryRepository.class);
        accountQueryPort = mock(AccountQueryPort.class);
        useCase = new GetAccountBalanceUseCase(snapshotRepository, entryRepository, accountQueryPort);
    }

    @Test
    void shouldReturnSnapshotBalanceWhenPresent() {
        GetAccountBalanceQuery query = new GetAccountBalanceQuery(10L, 100L);
        when(accountQueryPort.findAccountDetails(10L)).thenReturn(Optional.of(new AccountDetailsDto(10L, 100L, CurrencyCode.NGN, "ACTIVE")));

        BalanceSnapshot snapshot = new BalanceSnapshot(1L, 10L, Money.of("1000", CurrencyCode.NGN), 100L, ZonedDateTime.now());
        when(snapshotRepository.findLatestByAccountId(10L)).thenReturn(Optional.of(snapshot));

        Money result = useCase.execute(query);

        assertEquals(Money.of("1000.0000", CurrencyCode.NGN), result);
    }

    @Test
    void shouldReturnZeroWhenNoSnapshot() {
        GetAccountBalanceQuery query = new GetAccountBalanceQuery(10L, 100L);
        when(accountQueryPort.findAccountDetails(10L)).thenReturn(Optional.of(new AccountDetailsDto(10L, 100L, CurrencyCode.NGN, "ACTIVE")));

        when(snapshotRepository.findLatestByAccountId(10L)).thenReturn(Optional.empty());

        Money result = useCase.execute(query);

        assertEquals(Money.zero(CurrencyCode.NGN), result);
    }
}
