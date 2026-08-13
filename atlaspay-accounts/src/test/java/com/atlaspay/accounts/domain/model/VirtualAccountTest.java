package com.atlaspay.accounts.domain.model;

import com.atlaspay.accounts.domain.event.VirtualAccountCreatedEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualAccountTest {

    @Test
    void shouldCreateVirtualAccountSuccessfully() {
        VirtualAccount account = VirtualAccount.create(
                new Long("acc_1"),
                "mer_1",
                "Test Merchant Account",
                OwnerType.MERCHANT,
                "Wema",
                "idempotency_123"
        );
        
        assertEquals(AccountStatus.PENDING_ISSUANCE, account.getStatus());
        assertEquals("Wema", account.getBankName());
        assertEquals(1, account.pullDomainEvents().size());
        assertTrue(account.peekDomainEvents().isEmpty());
    }
}
