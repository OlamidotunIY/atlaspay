package com.atlaspay.ledger.application.usecase;

import com.atlaspay.ledger.application.command.PostLedgerTransactionCommand;
import com.atlaspay.ledger.application.command.PostLedgerTransactionCommand.EntryCommand;
import com.atlaspay.ledger.domain.model.EntryType;
import com.atlaspay.ledger.domain.model.LedgerTransaction;
import com.atlaspay.ledger.domain.model.SourceSystem;
import com.atlaspay.ledger.domain.repository.LedgerEntryRepository;
import com.atlaspay.ledger.domain.repository.LedgerTransactionRepository;
import com.atlaspay.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlaspay.ledger.domain.model.BalanceSnapshot;
import com.atlaspay.shared.exception.ConflictException;
import com.atlaspay.shared.money.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.atlaspay.shared.port.out.AccountQueryPort;
import com.atlaspay.shared.port.out.AccountDetailsDto;
import java.util.Optional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostLedgerTransactionUseCaseTest {

    private LedgerTransactionRepository repository;
    private LedgerEntryRepository entryRepository;
    private BalanceSnapshotRepository snapshotRepository;
    private AccountQueryPort accountQueryPort;
    private PostLedgerTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(LedgerTransactionRepository.class);
        entryRepository = mock(LedgerEntryRepository.class);
        snapshotRepository = mock(BalanceSnapshotRepository.class);
        accountQueryPort = mock(AccountQueryPort.class);
        useCase = new PostLedgerTransactionUseCase(repository, entryRepository, snapshotRepository, accountQueryPort);
    }

    @Test
    void shouldSuccessfullyPostBalancedTransaction() {
        PostLedgerTransactionCommand command = new PostLedgerTransactionCommand(
                "TX-100",
                SourceSystem.SYSTEM,
                100L,
                List.of(
                        new EntryCommand(1L, new BigDecimal("100.00"), CurrencyCode.NGN, EntryType.DEBIT, "Debit Account A"),
                        new EntryCommand(2L, new BigDecimal("100.00"), CurrencyCode.NGN, EntryType.CREDIT, "Credit Account B")
                )
        );

        when(repository.existsByReference("TX-100", SourceSystem.SYSTEM)).thenReturn(false);
        when(repository.nextIdentity()).thenReturn(999L);
        when(entryRepository.nextIdentity()).thenReturn(101L, 102L);
        when(accountQueryPort.findAccountDetails(1L)).thenReturn(Optional.of(new AccountDetailsDto(1L, 100L, CurrencyCode.NGN, "ACTIVE")));
        when(accountQueryPort.findAccountDetails(2L)).thenReturn(Optional.of(new AccountDetailsDto(2L, 100L, CurrencyCode.NGN, "ACTIVE")));
        
        when(snapshotRepository.findLatestByAccountIdForUpdate(1L)).thenReturn(Optional.empty());
        when(snapshotRepository.findLatestByAccountIdForUpdate(2L)).thenReturn(Optional.empty());

        useCase.execute(command);

        ArgumentCaptor<LedgerTransaction> captor = ArgumentCaptor.forClass(LedgerTransaction.class);
        verify(repository).save(captor.capture());

        LedgerTransaction savedTransaction = captor.getValue();
        assertEquals("TX-100", savedTransaction.getTransactionReference().transactionId());
        assertEquals(SourceSystem.SYSTEM, savedTransaction.getTransactionReference().sourceSystem());
        assertEquals(2, savedTransaction.getEntries().size());
    }

    @Test
    void shouldThrowConflictExceptionWhenIdempotencyKeyExists() {
        PostLedgerTransactionCommand command = new PostLedgerTransactionCommand(
                "TX-100",
                SourceSystem.SYSTEM,
                100L,
                List.of()
        );

        when(repository.existsByReference("TX-100", SourceSystem.SYSTEM)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> useCase.execute(command));
        assertEquals("Transaction already posted", ex.getMessage());
        assertEquals(com.atlaspay.ledger.domain.exception.LedgerErrorCode.TRANSACTION_ALREADY_POSTED, ex.getErrorCode());
        verify(repository, never()).save(any());
    }
}
