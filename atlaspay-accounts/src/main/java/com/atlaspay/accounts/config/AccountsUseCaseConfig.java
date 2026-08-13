package com.atlaspay.accounts.config;

import com.atlaspay.accounts.application.port.out.VirtualAccountQueryService;
import com.atlaspay.accounts.application.usecase.ActivateVirtualAccountUseCase;
import com.atlaspay.accounts.application.usecase.IssueVirtualAccountUseCase;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountsUseCaseConfig {

    @Bean
    public IssueVirtualAccountUseCase issueVirtualAccountUseCase(
            VirtualAccountDomainRepository repository,
            VirtualAccountQueryService queryService,
            DomainEventPublisher eventPublisher) {
        return new IssueVirtualAccountUseCase(repository, queryService, eventPublisher);
    }

    @Bean
    public ActivateVirtualAccountUseCase activateVirtualAccountUseCase(
            VirtualAccountDomainRepository repository,
            DomainEventPublisher eventPublisher) {
        return new ActivateVirtualAccountUseCase(repository, eventPublisher);
    }

    @Bean
    public com.atlaspay.accounts.application.usecase.ForceCloseAccountsUseCase forceCloseAccountsUseCase(
            VirtualAccountDomainRepository repository,
            DomainEventPublisher eventPublisher) {
        return new com.atlaspay.accounts.application.usecase.ForceCloseAccountsUseCase(repository, eventPublisher);
    }
    
    @Bean
    public com.atlaspay.accounts.application.usecase.RequestClosureUseCase requestClosureUseCase(
            VirtualAccountDomainRepository repository,
            DomainEventPublisher eventPublisher) {
        return new com.atlaspay.accounts.application.usecase.RequestClosureUseCase(repository, eventPublisher);
    }
    
    @Bean
    public com.atlaspay.accounts.application.usecase.GetVirtualAccountsUseCase getVirtualAccountsUseCase(
            VirtualAccountDomainRepository repository) {
        return new com.atlaspay.accounts.application.usecase.GetVirtualAccountsUseCase(repository);
    }
}
