package com.atlaspay.accounts.application.saga;

import com.atlaspay.accounts.application.dto.AccountIssuanceRequestDto;
import com.atlaspay.accounts.application.port.out.AccountIssuancePort;
import com.atlaspay.accounts.domain.event.VirtualAccountCreatedEvent;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
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
        
        repository.findById(Long.valueOf(event.aggregateId())).ifPresent(account -> {
            AccountIssuanceRequestDto request = new AccountIssuanceRequestDto(
                    String.valueOf(account.getId()),
                    account.getAccountName(),
                    account.getBankName()
            );
            
            accountIssuancePort.issueVirtualAccount(request);
        });
    }
}
