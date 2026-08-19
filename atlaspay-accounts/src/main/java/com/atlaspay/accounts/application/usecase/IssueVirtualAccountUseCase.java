package com.atlaspay.accounts.application.usecase;

import com.atlaspay.accounts.application.command.IssueVirtualAccountCommand;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;

import com.atlaspay.accounts.application.port.out.VirtualAccountQueryService;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.accounts.domain.exception.AccountsErrorCode;

@RequiredArgsConstructor
public class IssueVirtualAccountUseCase extends BaseUseCase<IssueVirtualAccountCommand, Long> {

    private final VirtualAccountDomainRepository repository;
    private final VirtualAccountQueryService queryService;
    private final DomainEventPublisher eventPublisher;

    @Override
    public Long execute(IssueVirtualAccountCommand command) {
        
        if (queryService.countByIntegration(command.integration()) >= 2) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Merchant can have at most 2 accounts");
        }
        if (queryService.existsByIntegrationAndBankName(command.integration(), command.bankName())) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Merchant already has an account with " + command.bankName());
        }

        VirtualAccount account = VirtualAccount.create(repository.nextIdentity(), 
                command.integration(), 
                command.customerCode(), 
                command.accountName(), 
                command.bankName(),
                command.idempotencyKey(),
                command.currency()
        );
        
        VirtualAccount savedAccount = repository.save(account);
        publishEvents(savedAccount, eventPublisher);
        
        return savedAccount.getId();
    }
}
