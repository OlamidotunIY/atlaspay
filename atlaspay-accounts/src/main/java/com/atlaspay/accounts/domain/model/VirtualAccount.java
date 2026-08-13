package com.atlaspay.accounts.domain.model;

import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.domain.id.VirtualAccountId;
import com.atlaspay.shared.domain.valueobject.NUBAN;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.accounts.domain.exception.AccountsErrorCode;
import com.atlaspay.accounts.domain.event.*;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class VirtualAccount extends AggregateRoot<VirtualAccountId> {

    private final VirtualAccountId id;
    private final String ownerId;
    private final String accountName;
    private final OwnerType ownerType;
    private final String bankName;
    private AccountStatus status;
    private NUBAN nuban;

    public VirtualAccount(VirtualAccountId id, String ownerId, String accountName, OwnerType ownerType, String bankName, AccountStatus status, NUBAN nuban) {
        this.id = id;
        this.ownerId = ownerId;
        this.accountName = accountName;
        this.ownerType = ownerType;
        this.bankName = bankName;
        this.status = status;
        this.nuban = nuban;
    }

    public static VirtualAccount create(VirtualAccountId id, String ownerId, String accountName, OwnerType ownerType, String bankName) {
        VirtualAccount account = new VirtualAccount(id, ownerId, accountName, ownerType, bankName, AccountStatus.PENDING_ISSUANCE, null);
        account.registerEvent(new VirtualAccountCreatedEvent(
                UUID.randomUUID().toString(),
                id.value(),
                ZonedDateTime.now(),
                null
        ));
        return account;
    }

    public void assignNuban(NUBAN nuban) {
        if (this.status != AccountStatus.PENDING_ISSUANCE) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Account must be in PENDING_ISSUANCE state to assign a NUBAN");
        }
        if (nuban == null) {
            throw new IllegalArgumentException("NUBAN cannot be null");
        }
        this.nuban = nuban;
        this.status = AccountStatus.ACTIVE;
        registerEvent(new VirtualAccountActivatedEvent(
                UUID.randomUUID().toString(),
                id.value(),
                ZonedDateTime.now(),
                nuban.value()
        ));
    }

    public void requestClosure() {
        if (this.status == AccountStatus.CLOSED || this.status == AccountStatus.CLOSURE_REQUESTED) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Account is already closed or closure is requested");
        }
        this.status = AccountStatus.CLOSURE_REQUESTED;
        registerEvent(new VirtualAccountClosureRequestedEvent(
                UUID.randomUUID().toString(),
                id.value(),
                ZonedDateTime.now(),
                null
        ));
    }

    public void close() {
        if (this.status == AccountStatus.CLOSED) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Account is already closed");
        }
        this.status = AccountStatus.CLOSED;
        registerEvent(new VirtualAccountClosedEvent(
                UUID.randomUUID().toString(),
                id.value(),
                ZonedDateTime.now(),
                null
        ));
    }

    @Override
    public VirtualAccountId getId() {
        return id;
    }
}
