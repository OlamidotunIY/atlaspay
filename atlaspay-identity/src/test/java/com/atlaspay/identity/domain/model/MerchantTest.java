package com.atlaspay.identity.domain.model;

import com.atlaspay.identity.domain.event.MerchantComplianceStepCompleted;
import com.atlaspay.identity.domain.event.MerchantComplianceSubmitted;
import com.atlaspay.identity.domain.event.MerchantRegistered;
import com.atlaspay.identity.domain.model.BusinessType;
import com.atlaspay.identity.domain.model.ComplianceStatus;
import com.atlaspay.identity.domain.model.GovernmentIdType;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.model.StaffSize;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MerchantTest {

    @Test
    void shouldRegisterSuccessfully() {
        Merchant merchant = new Merchant(new MerchantId("mer_1"), "NG", "Test Inc", "John", "Doe", new EmailAddress("test@atlaspay.com"), new PhoneNumber("+2348000000000"), "hashedPass", BusinessType.REGISTERED);
        
        assertEquals(ComplianceStatus.NOT_STARTED, merchant.getComplianceStatus());
        assertEquals("Test Inc", merchant.getBusinessName());
        assertEquals(1, merchant.pullDomainEvents().size());
    }

    @Test
    void shouldCompleteComplianceStep() {
        Merchant merchant = new Merchant(new MerchantId("mer_1"), "NG", "Test Inc", "John", "Doe", new EmailAddress("test@atlaspay.com"), new PhoneNumber("+2348000000000"), "hashedPass", BusinessType.REGISTERED);
        merchant.pullDomainEvents(); // clear initial events
        
        merchant.updateComplianceProfile("Desc", StaffSize.ONE_TO_TEN, "IT", "Tech", java.math.BigDecimal.valueOf(1000), "NGN");
        assertEquals(1, merchant.pullDomainEvents().size());
        
        merchant.updateComplianceContact(new EmailAddress("support@test.com"), new EmailAddress("dispute@test.com"), null, null, null, null, null, null, "Lagos", "Ikeja", "Ikeja", "Street 1");
        merchant.updateComplianceOwner("12345678901", "12345678901", java.time.LocalDate.now(), "Address", GovernmentIdType.NIN_SLIP, "12345", "RC123");
        merchant.updateComplianceAccount("035", "1234567890", "Test Inc");
        
        // Final step should manually trigger submission
        merchant.acceptServiceAgreement();
        merchant.submitCompliance();
        
        assertEquals(ComplianceStatus.SUBMITTED, merchant.getComplianceStatus());
        
        var events = merchant.pullDomainEvents();
        assertTrue(events.stream().anyMatch(e -> e instanceof MerchantComplianceSubmitted));
    }
}
