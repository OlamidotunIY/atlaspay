package com.atlaspay.accounts.application.saga;

import com.atlaspay.accounts.application.dto.AccountIssuanceRequestDto;
import com.atlaspay.accounts.application.port.out.AccountIssuancePort;
import com.atlaspay.accounts.domain.event.VirtualAccountCreatedEvent;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.shared.domain.id.VirtualAccountId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountIssuanceSaga {

    private final AccountIssuancePort accountIssuancePort;
    private final VirtualAccountDomainRepository repository;

    @Async
    @EventListener
    public void on(VirtualAccountCreatedEvent event) {
        log.info("Saga triggered for account issuance. Reference: {}", event.aggregateId());
        
        repository.findById(new VirtualAccountId(event.aggregateId())).ifPresent(account -> {
            AccountIssuanceRequestDto request = new AccountIssuanceRequestDto(
                    account.getId().value(),
                    account.getAccountName(),
                    account.getBankName()
            );
            
            accountIssuancePort.issueVirtualAccount(request);
        });
    }
}
