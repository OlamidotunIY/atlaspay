package com.atlaspay.accounts.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualAccountTest {

    @Test
    void shouldCreateVirtualAccountSuccessfully() {
        VirtualAccount account = VirtualAccount.create(
                1L,
                100L,
                "CUST-1",
                "Test Merchant Account",
                "Wema",
                "idempotency_123",
                com.atlaspay.shared.money.CurrencyCode.NGN
        );
        
        assertEquals(AccountStatus.PENDING_ISSUANCE, account.getStatus());
        assertEquals("Wema", account.getBankName());
        assertEquals(1, account.pullDomainEvents().size());
        assertTrue(account.peekDomainEvents().isEmpty());
    }
}
