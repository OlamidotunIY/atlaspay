package com.atlaspay.accounts.application.usecase;

import com.atlaspay.accounts.application.command.RequestClosureCommand;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.accounts.domain.exception.AccountsErrorCode;
import com.atlaspay.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestClosureUseCase extends BaseUseCase<RequestClosureCommand, Void> {

    private final VirtualAccountDomainRepository repository;
    private final DomainEventPublisher eventPublisher;

    @Override
    public Void execute(RequestClosureCommand command) {
        VirtualAccount account = repository.findById(Long.valueOf(command.accountId()))
                .orElseThrow(() -> new NotFoundException(AccountsErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
        
        account.requestClosure();
        
        repository.save(account);
        publishEvents(account, eventPublisher);
        
        return null;
    }
}
