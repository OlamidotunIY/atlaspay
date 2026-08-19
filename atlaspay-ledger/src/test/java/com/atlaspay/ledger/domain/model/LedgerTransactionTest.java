package com.atlaspay.ledger.domain.model;

import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.money.CurrencyCode;
import com.atlaspay.shared.money.Money;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LedgerTransactionTest {

    @Test
    void shouldCreateTransactionWhenBalanced() {
        TransactionReference ref = new TransactionReference("TX-123", "TRANSFERS");
        
        LedgerEntry debit = new LedgerEntry(1L, 100L, Money.of("500.00", CurrencyCode.NGN), EntryType.DEBIT, ref, "Transfer out", Money.of("0.00", CurrencyCode.NGN), ZonedDateTime.now());
        LedgerEntry credit = new LedgerEntry(2L, 200L, Money.of("500.00", CurrencyCode.NGN), EntryType.CREDIT, ref, "Transfer in", Money.of("500.00", CurrencyCode.NGN), ZonedDateTime.now());
        
        LedgerTransaction transaction = new LedgerTransaction(10L, ref, List.of(debit, credit), ZonedDateTime.now());
        
        assertEquals(10L, transaction.getId());
        assertEquals(2, transaction.getEntries().size());
        assertEquals("TX-123", transaction.getTransactionReference().transactionId());
    }

    @Test
    void shouldThrowExceptionWhenUnbalanced() {
        TransactionReference ref = new TransactionReference("TX-124", "TRANSFERS");
        
        LedgerEntry debit = new LedgerEntry(3L, 100L, Money.of("500.00", CurrencyCode.NGN), EntryType.DEBIT, ref, "Transfer out", Money.of("0", CurrencyCode.NGN), ZonedDateTime.now());
        LedgerEntry credit = new LedgerEntry(4L, 200L, Money.of("400.00", CurrencyCode.NGN), EntryType.CREDIT, ref, "Transfer in", Money.of("400.00", CurrencyCode.NGN), ZonedDateTime.now());
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            new LedgerTransaction(11L, ref, List.of(debit, credit), ZonedDateTime.now());
        });
        
        assertEquals("Total debits must equal total credits", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLessThanTwoEntries() {
        TransactionReference ref = new TransactionReference("TX-125", "CHARGES");
        
        LedgerEntry debit = new LedgerEntry(5L, 100L, Money.of("500.00", CurrencyCode.NGN), EntryType.DEBIT, ref, "Charge", Money.of("0", CurrencyCode.NGN), ZonedDateTime.now());
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            new LedgerTransaction(12L, ref, List.of(debit), ZonedDateTime.now());
        });
        
        assertEquals("A ledger transaction must have at least two entries", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMissingCreditOrDebit() {
        TransactionReference ref = new TransactionReference("TX-126", "TRANSFERS");
        
        LedgerEntry debit1 = new LedgerEntry(6L, 100L, Money.of("250.00", CurrencyCode.NGN), EntryType.DEBIT, ref, "Split out 1", Money.of("250", CurrencyCode.NGN), ZonedDateTime.now());
        LedgerEntry debit2 = new LedgerEntry(7L, 200L, Money.of("250.00", CurrencyCode.NGN), EntryType.DEBIT, ref, "Split out 2", Money.of("0", CurrencyCode.NGN), ZonedDateTime.now());
        
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            new LedgerTransaction(13L, ref, List.of(debit1, debit2), ZonedDateTime.now());
        });
        
        assertEquals("A ledger transaction must have both debit and credit entries", exception.getMessage());
    }
}
