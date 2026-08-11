package com.atlaspay.identity.domain.model;

import com.atlaspay.identity.domain.event.*;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import com.atlaspay.shared.exception.BusinessRuleException;

import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.AccessLevel;

@Getter(AccessLevel.PACKAGE)
public class Merchant extends AggregateRoot<MerchantId> {

    private final MerchantId id;
    private final String country;
    private String businessName;
    private String firstName;
    private String lastName;
    private EmailAddress email;
    private PhoneNumber phone;
    private String hashedPassword;
    private final BusinessType businessType;
    private boolean emailVerified;
    private String emailVerificationToken;
    private ZonedDateTime emailVerificationTokenExpiresAt;
    private ComplianceStatus complianceStatus;
    private ComplianceStep complianceStep;
    private MerchantCompliance compliance;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Merchant(MerchantId id, String country, String businessName, String firstName, String lastName,
                    EmailAddress email, PhoneNumber phone, String hashedPassword, BusinessType businessType) {
        
        this.id = id;
        this.country = country;
        this.businessName = businessName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.hashedPassword = hashedPassword;
        this.businessType = businessType;
        this.emailVerified = false;
        
        this.emailVerificationToken = UUID.randomUUID().toString();
        this.emailVerificationTokenExpiresAt = ZonedDateTime.now().plusHours(24);
        
        this.complianceStatus = ComplianceStatus.NOT_STARTED;
        this.complianceStep = null;
        this.compliance = new MerchantCompliance();
        
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = this.createdAt;

        registerEvent(new MerchantRegistered(
            UUID.randomUUID().toString(),
            id.value(),
            ZonedDateTime.now(),
            null, // correlationId would be injected in production
            this.businessName,
            this.email.value(),
            this.country,
            this.businessType
        ));
    }

    public void verifyEmail(String token) {
        if (this.emailVerified) {
            throw new BusinessRuleException(IdentityErrorCode.EMAIL_ALREADY_VERIFIED, "Email is already verified");
        }
        if (this.emailVerificationToken == null || !this.emailVerificationToken.equals(token)) {
            throw new BusinessRuleException(IdentityErrorCode.EMAIL_TOKEN_INVALID_OR_EXPIRED, "Invalid verification token");
        }
        if (this.emailVerificationTokenExpiresAt != null && ZonedDateTime.now().isAfter(this.emailVerificationTokenExpiresAt)) {
            throw new BusinessRuleException(IdentityErrorCode.EMAIL_TOKEN_INVALID_OR_EXPIRED, "Verification token has expired");
        }
        
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationTokenExpiresAt = null;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new MerchantEmailVerified(
            UUID.randomUUID().toString(),
            id.value(),
            ZonedDateTime.now(),
            null
        ));
    }

    public void regenerateEmailVerificationToken() {
        if (this.emailVerified) {
            throw new BusinessRuleException(IdentityErrorCode.EMAIL_ALREADY_VERIFIED, "Email is already verified");
        }
        
        this.emailVerificationToken = UUID.randomUUID().toString();
        this.emailVerificationTokenExpiresAt = ZonedDateTime.now().plusHours(24);
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new MerchantEmailVerificationResent(
            UUID.randomUUID().toString(),
            id.value(),
            ZonedDateTime.now(),
            null
        ));
    }
    
    public void completeComplianceStep(ComplianceStep step) {
        if (this.complianceStatus == ComplianceStatus.NOT_STARTED) {
            this.complianceStatus = ComplianceStatus.IN_PROGRESS;
        }
        
        if (this.complianceStep == null && step != ComplianceStep.PROFILE) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_STEP_OUT_OF_ORDER, "First step must be PROFILE");
        }
        
        if (this.complianceStep != null && step != this.complianceStep.next() && step != this.complianceStep) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_STEP_OUT_OF_ORDER, "Steps must be completed in order");
        }
        
        this.complianceStep = step;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new MerchantComplianceStepCompleted(
            UUID.randomUUID().toString(),
            id.value(),
            ZonedDateTime.now(),
            null,
            step
        ));
    }

    public void submitCompliance() {
        if (this.complianceStep != ComplianceStep.SERVICE_AGREEMENT || !this.compliance.isAgreedToTerms()) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_NOT_ALL_STEPS_COMPLETE, "All 5 compliance steps must be completed before submission");
        }
        
        this.complianceStatus = ComplianceStatus.SUBMITTED;
        this.updatedAt = ZonedDateTime.now();
        
        registerEvent(new MerchantComplianceSubmitted(
            UUID.randomUUID().toString(),
            id.value(),
            ZonedDateTime.now(),
            null
        ));
    }

    public void approveCompliance() {
        if (this.complianceStatus != ComplianceStatus.SUBMITTED && this.complianceStatus != ComplianceStatus.UNDER_REVIEW) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_NOT_SUBMITTED, "Cannot approve compliance that hasn't been submitted");
        }
        
        this.complianceStatus = ComplianceStatus.APPROVED;
        this.updatedAt = ZonedDateTime.now();
        
        registerEvent(new MerchantComplianceApproved(
            UUID.randomUUID().toString(),
            id.value(),
            ZonedDateTime.now(),
            null
        ));
    }

    public void rejectCompliance(String reason) {
        if (this.complianceStatus != ComplianceStatus.SUBMITTED && this.complianceStatus != ComplianceStatus.UNDER_REVIEW) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_NOT_SUBMITTED, "Cannot reject compliance that hasn't been submitted");
        }
        
        this.complianceStatus = ComplianceStatus.REJECTED;
        this.updatedAt = ZonedDateTime.now();
        
        registerEvent(new MerchantComplianceRejected(
            UUID.randomUUID().toString(),
            id.value(),
            ZonedDateTime.now(),
            null,
            reason
        ));
    }

    public void updateProfile(String businessName, PhoneNumber phone) {
        this.businessName = businessName;
        this.phone = phone;
        this.updatedAt = ZonedDateTime.now();
        
        registerEvent(new MerchantProfileUpdated(
            UUID.randomUUID().toString(),
            id.value(),
            ZonedDateTime.now(),
            null
        ));
    }

    @Override
    public MerchantId getId() {
        return id;
    }
    
    public MerchantCompliance getCompliance() {
        return compliance;
    }
    
    public ComplianceStatus getComplianceStatus() {
        return complianceStatus;
    }
}
