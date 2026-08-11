package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.usecase.BaseUseCase;

import com.atlaspay.identity.application.dto.RegisterSubAccountResult;
import com.atlaspay.identity.application.port.out.AccountResolutionService;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.SubAccount;
import com.atlaspay.identity.domain.repository.SubAccountRepository;
import com.atlaspay.shared.domain.id.SubAccountId;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.ConflictException;
import com.atlaspay.shared.exception.NotFoundException;

public class RegisterSubAccountUseCase extends BaseUseCase<RegisterSubAccountCommand, RegisterSubAccountResult> {

    private final SubAccountRepository subAccountRepository;
    private final AccountResolutionService accountResolutionService;
    private final DomainEventPublisher eventPublisher;

    public RegisterSubAccountUseCase(
            SubAccountRepository subAccountRepository,
            AccountResolutionService accountResolutionService,
            DomainEventPublisher eventPublisher) {
        this.subAccountRepository = subAccountRepository;
        this.accountResolutionService = accountResolutionService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RegisterSubAccountResult execute(RegisterSubAccountCommand command) {
        if (subAccountRepository.findByMerchantIdAndBankCodeAndAccountNumber(
                command.merchantId(), command.bankCode(), command.accountNumber()).isPresent()) {
            throw new ConflictException(IdentityErrorCode.SUBACCOUNT_ALREADY_EXISTS, "SubAccount with this bank code and account number already exists for this merchant");
        }

        String accountName = accountResolutionService.resolveAccountName(command.bankCode(), command.accountNumber())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.BANK_ACCOUNT_NOT_FOUND, "Bank account could not be resolved"));

        SubAccount subAccount = new SubAccount(
            SubAccountId.generate(),
            command.merchantId(),
            command.bankCode(),
            command.accountNumber(),
            accountName,
            command.description()
        );

        subAccountRepository.save(subAccount);

        publishEvents(subAccount, eventPublisher);

        return new RegisterSubAccountResult(subAccount.getId(), accountName);
    }
}
