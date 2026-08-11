package com.atlaspay.identity.domain.model;

import com.atlaspay.identity.domain.event.SubAccountDeactivated;
import com.atlaspay.identity.domain.event.SubAccountRegistered;
import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.id.SubAccountId;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter(AccessLevel.PACKAGE)
public class SubAccount extends AggregateRoot<SubAccountId> {

    private final SubAccountId id;
    private final MerchantId merchantId;
    private final String bankCode;
    private final String accountNumber;
    private final String accountName;
    private final String description;
    private boolean active;
    private final ZonedDateTime createdAt;

    public SubAccount(SubAccountId id, MerchantId merchantId, String bankCode, String accountNumber, String accountName, String description) {
        if (id == null) throw new IllegalArgumentException("SubAccount ID is required");
        if (merchantId == null) throw new IllegalArgumentException("Merchant ID is required");
        if (bankCode == null || bankCode.isBlank()) throw new IllegalArgumentException("Bank code is required");
        if (accountNumber == null || accountNumber.isBlank()) throw new IllegalArgumentException("Account number is required");
        if (accountName == null || accountName.isBlank()) throw new IllegalArgumentException("Account name is required");

        this.id = id;
        this.merchantId = merchantId;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.description = description;
        this.active = true;
        this.createdAt = ZonedDateTime.now();

        registerEvent(new SubAccountRegistered(
            UUID.randomUUID().toString(),
            this.id.value(),
            this.createdAt,
            new SubAccountRegistered.Payload(
                this.merchantId.value(),
                this.bankCode,
                this.accountNumber,
                this.accountName
            )
        ));
    }

    public void deactivate() {
        if (!this.active) {
            throw new BusinessRuleException(IdentityErrorCode.SUBACCOUNT_ALREADY_INACTIVE, "SubAccount is already inactive");
        }
        
        this.active = false;

        registerEvent(new SubAccountDeactivated(
            UUID.randomUUID().toString(),
            this.id.value(),
            ZonedDateTime.now(),
            new SubAccountDeactivated.Payload(this.merchantId.value())
        ));
    }

    @Override
    public SubAccountId getId() {
        return id;
    }
}
