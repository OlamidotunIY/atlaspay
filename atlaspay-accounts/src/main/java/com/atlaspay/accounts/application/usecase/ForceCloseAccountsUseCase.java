package com.atlaspay.accounts.application.usecase;

import com.atlaspay.accounts.application.command.ForceCloseAccountsCommand;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class ForceCloseAccountsUseCase extends BaseUseCase<ForceCloseAccountsCommand, Void> {

    private final VirtualAccountDomainRepository repository;
    private final DomainEventPublisher eventPublisher;

    @Override
    public Void execute(ForceCloseAccountsCommand command) {
        List<VirtualAccount> accounts = repository.findByIntegration(command.integration());
        
        for (VirtualAccount account : accounts) {
            account.close();
            repository.save(account);
            publishEvents(account, eventPublisher);
        }
        
        return null;
    }
}
