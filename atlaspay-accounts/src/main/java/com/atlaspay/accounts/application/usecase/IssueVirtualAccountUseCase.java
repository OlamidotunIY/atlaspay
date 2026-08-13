package com.atlaspay.accounts.application.usecase;

import com.atlaspay.accounts.application.command.IssueVirtualAccountCommand;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.shared.domain.id.VirtualAccountId;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import com.atlaspay.accounts.domain.model.OwnerType;
import com.atlaspay.accounts.application.port.out.VirtualAccountQueryService;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.accounts.domain.exception.AccountsErrorCode;

@RequiredArgsConstructor
public class IssueVirtualAccountUseCase extends BaseUseCase<IssueVirtualAccountCommand, VirtualAccountId> {

    private final VirtualAccountDomainRepository repository;
    private final VirtualAccountQueryService queryService;
    private final DomainEventPublisher eventPublisher;

    @Override
    public VirtualAccountId execute(IssueVirtualAccountCommand command) {
        if (command.ownerType() == OwnerType.MERCHANT) {
            if (queryService.countByOwnerId(command.ownerId()) >= 2) {
                throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Merchant can have at most 2 accounts");
            }
            if (queryService.existsByOwnerIdAndBankName(command.ownerId(), command.bankName())) {
                throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Merchant already has an account with " + command.bankName());
            }
        } else if (command.ownerType() == OwnerType.CUSTOMER) {
            if (queryService.countByOwnerId(command.ownerId()) >= 1) {
                throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Customer can have at most 1 account");
            }
        }

        VirtualAccountId accountId = new VirtualAccountId(UUID.randomUUID().toString());
        VirtualAccount account = VirtualAccount.create(
                accountId, 
                command.ownerId(), 
                command.ownerId() + " Account", 
                command.ownerType(), 
                command.bankName(),
                command.idempotencyKey()
        );
        
        VirtualAccount savedAccount = repository.save(account);
        publishEvents(savedAccount, eventPublisher);
        
        return savedAccount.getId();
    }
}
