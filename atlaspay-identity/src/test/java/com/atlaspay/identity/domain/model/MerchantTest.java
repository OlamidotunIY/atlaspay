package com.atlaspay.identity.domain.model;

import com.atlaspay.identity.domain.event.MerchantComplianceStepCompleted;
import com.atlaspay.identity.domain.event.MerchantComplianceSubmitted;
import com.atlaspay.identity.domain.event.MerchantRegistered;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.valueobject.Country;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MerchantTest {

    @Test
    void shouldRegisterSuccessfully() {
        Merchant merchant = Merchant.register(new MerchantId("mer_1"), new EmailAddress("test@atlaspay.com"), "hashedPass", "Test Inc", Country.NG);
        
        assertEquals(MerchantStatus.PENDING_COMPLIANCE, merchant.getStatus());
        assertEquals("Test Inc", merchant.getBusinessName());
        assertEquals(1, merchant.pullDomainEvents().size());
    }

    @Test
    void shouldCompleteComplianceStep() {
        Merchant merchant = Merchant.register(new MerchantId("mer_1"), new EmailAddress("test@atlaspay.com"), "hashedPass", "Test Inc", Country.NG);
        merchant.pullDomainEvents(); // clear initial events
        
        merchant.completeComplianceStep(ComplianceStep.BUSINESS_DETAILS, "{}");
        assertEquals(1, merchant.pullDomainEvents().size());
        
        merchant.completeComplianceStep(ComplianceStep.DIRECTOR_INFO, "{}");
        merchant.completeComplianceStep(ComplianceStep.BANK_ACCOUNT, "{}");
        merchant.completeComplianceStep(ComplianceStep.DOCUMENT_UPLOAD, "{}");
        
        // Final step should automatically trigger submission
        merchant.completeComplianceStep(ComplianceStep.FINAL_REVIEW, "{}");
        
        assertEquals(ComplianceStatus.SUBMITTED, merchant.getCompliance().getStatus());
        
        var events = merchant.pullDomainEvents();
        // 4 steps + final step + submitted event = 6
        assertTrue(events.stream().anyMatch(e -> e instanceof MerchantComplianceSubmitted));
    }
}
