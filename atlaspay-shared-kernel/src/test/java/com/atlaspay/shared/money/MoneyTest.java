package com.atlaspay.shared.money;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithCorrectScale() {
        Money m = Money.of("10.5", CurrencyCode.NGN);
        assertEquals(new BigDecimal("10.5000"), m.amount());
        assertEquals(CurrencyCode.NGN, m.currency());
    }

    @Test
    void shouldAddSameCurrency() {
        Money m1 = Money.of("10.5", CurrencyCode.NGN);
        Money m2 = Money.of("5.5", CurrencyCode.NGN);
        Money sum = m1.add(m2);
        
        assertEquals(new BigDecimal("16.0000"), sum.amount());
    }

    @Test
    void shouldThrowWhenAddingDifferentCurrencies() {
        Money m1 = Money.of("10", CurrencyCode.NGN);
        Money m2 = Money.of("5", CurrencyCode.USD);
        
        assertThrows(IllegalArgumentException.class, () -> m1.add(m2));
    }

    @Test
    void shouldPerformBankersRounding() {
        // 10.55555 -> 10.5556
        Money m1 = Money.of("10.55555", CurrencyCode.NGN);
        assertEquals(new BigDecimal("10.5556"), m1.amount());

        // 10.55545 -> 10.5554
        Money m2 = Money.of("10.55545", CurrencyCode.NGN);
        assertEquals(new BigDecimal("10.5554"), m2.amount());
    }

    @Test
    void shouldCompareCorrectly() {
        Money m1 = Money.of("10", CurrencyCode.NGN);
        Money m2 = Money.of("20", CurrencyCode.NGN);
        
        assertTrue(m1.isLessThan(m2));
        assertTrue(m2.isGreaterThan(m1));
        assertFalse(m1.isGreaterThan(m2));
        assertEquals(0, m1.compareTo(Money.of("10", CurrencyCode.NGN)));
    }
}
