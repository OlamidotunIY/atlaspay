package com.atlaspay.accounts.application.usecase;

import com.atlaspay.accounts.application.command.IssueVirtualAccountCommand;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.shared.domain.id.VirtualAccountId;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class IssueVirtualAccountUseCase extends BaseUseCase<IssueVirtualAccountCommand, VirtualAccountId> {

    private final VirtualAccountDomainRepository repository;
    private final DomainEventPublisher eventPublisher;

    @Override
    public VirtualAccountId execute(IssueVirtualAccountCommand command) {
        VirtualAccountId accountId = new VirtualAccountId(UUID.randomUUID().toString());
        VirtualAccount account = VirtualAccount.create(
                accountId, 
                command.ownerId(), 
                command.ownerId() + " Account", 
                command.ownerType(), 
                command.bankName()
        );
        
        VirtualAccount savedAccount = repository.save(account);
        publishEvents(savedAccount, eventPublisher);
        
        return savedAccount.getId();
    }
}
