package com.atlaspay.identity.domain.model;

import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import lombok.AccessLevel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Child entity holding all compliance data; populated as steps are completed.
 * Kept separate to ensure Merchant aggregate doesn't get bloated.
 */
@Getter(AccessLevel.PACKAGE)
public class MerchantCompliance {

    // Step 1 - Profile fields
    private String description;
    private StaffSize staffSize;
    private String industry;
    private String category;
    private BigDecimal annualProjectedSalesVolume;
    private String annualProjectedSalesCurrency;

    // Step 2 - Contact fields
    private EmailAddress supportEmail;
    private EmailAddress disputeEmail;
    private PhoneNumber whatsappPhone;
    private String whatsappName;
    private String websiteUrl;
    private String twitterHandle;
    private String facebookUsername;
    private String instagramHandle;
    private String businessState;
    private String businessLga;
    private String businessCity;
    private String businessStreet;

    // Step 3 - Owner fields
    private String ownerBvn;
    private String ownerNin;
    private LocalDate ownerDateOfBirth;
    private String ownerAddress;
    private GovernmentIdType ownerIdType;
    private String ownerIdNumber;
    private String rcNumber;

    // Step 4 - Account fields
    private String settlementBankCode;
    private String settlementAccountNumber;
    private String settlementAccountName;

    // Step 5 - Service Agreement fields
    private boolean agreedToTerms;
    private ZonedDateTime agreementSignedAt;

    public MerchantCompliance() {
        this.agreedToTerms = false;
    }

    public void updateProfileStep(String description, StaffSize staffSize, String industry, String category, BigDecimal annualProjectedSalesVolume, String annualProjectedSalesCurrency) {
        this.description = description;
        this.staffSize = staffSize;
        this.industry = industry;
        this.category = category;
        this.annualProjectedSalesVolume = annualProjectedSalesVolume;
        this.annualProjectedSalesCurrency = annualProjectedSalesCurrency;
    }

    public void updateContactStep(EmailAddress supportEmail, EmailAddress disputeEmail, PhoneNumber whatsappPhone, String whatsappName, String websiteUrl, String twitterHandle, String facebookUsername, String instagramHandle, String businessState, String businessLga, String businessCity, String businessStreet) {
        this.supportEmail = supportEmail;
        this.disputeEmail = disputeEmail;
        this.whatsappPhone = whatsappPhone;
        this.whatsappName = whatsappName;
        this.websiteUrl = websiteUrl;
        this.twitterHandle = twitterHandle;
        this.facebookUsername = facebookUsername;
        this.instagramHandle = instagramHandle;
        this.businessState = businessState;
        this.businessLga = businessLga;
        this.businessCity = businessCity;
        this.businessStreet = businessStreet;
    }

    public void updateOwnerStep(String ownerBvn, String ownerNin, LocalDate ownerDateOfBirth, String ownerAddress, GovernmentIdType ownerIdType, String ownerIdNumber, String rcNumber) {
        this.ownerBvn = ownerBvn;
        this.ownerNin = ownerNin;
        this.ownerDateOfBirth = ownerDateOfBirth;
        this.ownerAddress = ownerAddress;
        this.ownerIdType = ownerIdType;
        this.ownerIdNumber = ownerIdNumber;
        this.rcNumber = rcNumber;
    }

    public void updateAccountStep(String settlementBankCode, String settlementAccountNumber, String settlementAccountName) {
        this.settlementBankCode = settlementBankCode;
        this.settlementAccountNumber = settlementAccountNumber;
        this.settlementAccountName = settlementAccountName;
    }

    public void acceptServiceAgreement(ZonedDateTime signedAt) {
        this.agreedToTerms = true;
        this.agreementSignedAt = signedAt;
    }
}
