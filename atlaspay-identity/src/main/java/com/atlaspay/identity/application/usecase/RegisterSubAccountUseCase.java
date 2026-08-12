package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.command.RegisterSubAccountCommand;
import com.atlaspay.identity.application.dto.SubAccountDto;
import com.atlaspay.identity.application.port.out.AccountNameResolutionPort;
import com.atlaspay.identity.domain.model.SubAccount;
import com.atlaspay.identity.domain.repository.SubAccountRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Component;

import com.atlaspay.identity.application.dto.RegisterSubAccountResult;

@Component
public class RegisterSubAccountUseCase extends BaseUseCase<RegisterSubAccountCommand, RegisterSubAccountResult> {

    private final SubAccountRepository subAccountRepository;
    private final AccountNameResolutionPort accountNameResolutionPort;
    private final DomainEventPublisher eventPublisher;

    public RegisterSubAccountUseCase(SubAccountRepository subAccountRepository, 
                                     AccountNameResolutionPort accountNameResolutionPort, 
                                     DomainEventPublisher eventPublisher) {
        this.subAccountRepository = subAccountRepository;
        this.accountNameResolutionPort = accountNameResolutionPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RegisterSubAccountResult execute(RegisterSubAccountCommand input) {
        String accountName = accountNameResolutionPort.resolve(input.bankCode(), input.accountNumber());
        
        SubAccount subAccount = new SubAccount(
                new com.atlaspay.shared.domain.id.SubAccountId(java.util.UUID.randomUUID().toString()),
                input.merchantId(),
                input.bankCode(),
                input.accountNumber(),
                accountName,
                input.description()
        );
        
        subAccountRepository.save(subAccount);
        publishEvents(subAccount, eventPublisher);
        
        return new RegisterSubAccountResult(subAccount.getId(), accountName);
    }
}
