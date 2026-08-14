package com.atlaspay.ledger.application.usecase;

import com.atlaspay.ledger.application.command.PostLedgerTransactionCommand;
import com.atlaspay.ledger.application.command.PostLedgerTransactionCommand.EntryCommand;
import com.atlaspay.ledger.domain.model.EntryType;
import com.atlaspay.ledger.domain.model.LedgerTransaction;
import com.atlaspay.ledger.domain.repository.LedgerEntryRepository;
import com.atlaspay.ledger.domain.repository.LedgerTransactionRepository;
import com.atlaspay.shared.exception.ConflictException;
import com.atlaspay.shared.money.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PostLedgerTransactionUseCaseTest {

    private LedgerTransactionRepository repository;
    private LedgerEntryRepository entryRepository;
    private PostLedgerTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(LedgerTransactionRepository.class);
        entryRepository = mock(LedgerEntryRepository.class);
        useCase = new PostLedgerTransactionUseCase(repository, entryRepository);
    }

    @Test
    void shouldSuccessfullyPostBalancedTransaction() {
        PostLedgerTransactionCommand command = new PostLedgerTransactionCommand(
                "TX-100",
                "TRANSFERS",
                List.of(
                        new EntryCommand(1L, new BigDecimal("100.00"), CurrencyCode.NGN, EntryType.DEBIT, "Debit Account A"),
                        new EntryCommand(2L, new BigDecimal("100.00"), CurrencyCode.NGN, EntryType.CREDIT, "Credit Account B")
                )
        );

        when(repository.existsByReference("TX-100", "TRANSFERS")).thenReturn(false);
        when(repository.nextIdentity()).thenReturn(999L);
        when(entryRepository.nextIdentity()).thenReturn(101L, 102L);

        useCase.execute(command);

        ArgumentCaptor<LedgerTransaction> captor = ArgumentCaptor.forClass(LedgerTransaction.class);
        verify(repository).save(captor.capture());

        LedgerTransaction savedTransaction = captor.getValue();
        assertEquals("TX-100", savedTransaction.getTransactionReference().transactionId());
        assertEquals("TRANSFERS", savedTransaction.getTransactionReference().sourceSystem());
        assertEquals(2, savedTransaction.getEntries().size());
    }

    @Test
    void shouldThrowConflictExceptionWhenIdempotencyKeyExists() {
        PostLedgerTransactionCommand command = new PostLedgerTransactionCommand(
                "TX-100",
                "TRANSFERS",
                List.of()
        );

        when(repository.existsByReference("TX-100", "TRANSFERS")).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> useCase.execute(command));
        assertEquals("Transaction already posted", ex.getMessage());
        assertEquals(com.atlaspay.ledger.domain.exception.LedgerErrorCode.TRANSACTION_ALREADY_POSTED, ex.getErrorCode());
        verify(repository, never()).save(any());
    }
}
