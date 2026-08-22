package com.atlaspay.accounts.application.usecase;

import org.springframework.stereotype.Service;

import com.atlaspay.accounts.application.command.ActivateVirtualAccountCommand;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.shared.domain.valueobject.NUBAN;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.accounts.domain.exception.AccountsErrorCode;
import com.atlaspay.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ActivateVirtualAccountUseCase extends BaseUseCase<ActivateVirtualAccountCommand, Void> {

    private final VirtualAccountDomainRepository repository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(ActivateVirtualAccountCommand command) {
        VirtualAccount account = repository.findById(Long.valueOf(command.referenceId()))
                .orElseThrow(() -> new NotFoundException(AccountsErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
        
        account.activate(new NUBAN(command.nuban()));
        
        repository.save(account);
        publishEvents(account, eventPublisher);
        
        return null;
    }
}



