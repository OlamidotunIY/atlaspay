package com.atlaspay.identity.domain.model;

import com.atlaspay.identity.domain.event.SubAccountDeactivated;
import com.atlaspay.identity.domain.event.SubAccountRegistered;
import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.SharedErrorCode;
import com.atlaspay.shared.exception.ValidationException;
import com.atlaspay.shared.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class SubAccount extends AggregateRoot<Long> {

    private final Long id;
    private final Long merchantId;
    private final String bankCode;
    private final String accountNumber;
    private final String accountName;
    private final String description;
    private boolean active;
    private final ZonedDateTime createdAt;

    public SubAccount(Long id, Long merchantId, String bankCode, String accountNumber, String accountName, String description) {
                if (merchantId == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Merchant ID is required");
        if (bankCode == null || bankCode.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Bank code is required");
        if (accountNumber == null || accountNumber.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Account number is required");
        if (accountName == null || accountName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Account name is required");

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
            id != null ? String.valueOf(id) : null,
            this.createdAt,
            new SubAccountRegistered.Payload(
                String.valueOf(merchantId),
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
            id != null ? String.valueOf(id) : null,
            ZonedDateTime.now(),
            new SubAccountDeactivated.Payload(String.valueOf(merchantId))
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
