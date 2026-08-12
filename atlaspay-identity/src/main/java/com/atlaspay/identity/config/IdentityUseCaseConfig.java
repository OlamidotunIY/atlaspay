package com.atlaspay.identity.config;

import com.atlaspay.identity.application.port.*;
import com.atlaspay.identity.application.usecase.*;
import com.atlaspay.identity.domain.repository.*;
import com.atlaspay.shared.event.DomainEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityUseCaseConfig {

    // Merchant / Registration Use Cases
    @Bean
    public RegisterMerchantUseCase registerMerchantUseCase(
            MerchantRepository merchantRepository, PasswordEncoder passwordEncoder, 
            GenerateTestApiKeyPairUseCase generateTestApiKeyPairUseCase, DomainEventPublisher eventPublisher) {
        return new RegisterMerchantUseCase(merchantRepository, passwordEncoder, generateTestApiKeyPairUseCase, eventPublisher);
    }

    @Bean
    public GetMerchantProfileUseCase getMerchantProfileUseCase(MerchantQueryService queryService) {
        return new GetMerchantProfileUseCase(queryService);
    }
    
    @Bean
    public VerifyMerchantEmailUseCase verifyMerchantEmailUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        return new VerifyMerchantEmailUseCase(merchantRepository, eventPublisher);
    }
    
    // Compliance Use Cases
    @Bean
    public CompleteComplianceProfileUseCase completeComplianceProfileUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        return new CompleteComplianceProfileUseCase(merchantRepository, eventPublisher);
    }
    
    @Bean
    public CompleteComplianceContactUseCase completeComplianceContactUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        return new CompleteComplianceContactUseCase(merchantRepository, eventPublisher);
    }
    
    @Bean
    public CompleteComplianceOwnerUseCase completeComplianceOwnerUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        return new CompleteComplianceOwnerUseCase(merchantRepository, eventPublisher);
    }
    
    @Bean
    public CompleteComplianceAccountUseCase completeComplianceAccountUseCase(
            MerchantRepository merchantRepository, AccountNameResolutionPort accountNameResolutionPort, DomainEventPublisher eventPublisher) {
        return new CompleteComplianceAccountUseCase(merchantRepository, accountNameResolutionPort, eventPublisher);
    }
    
    @Bean
    public CompleteComplianceServiceAgreementUseCase completeComplianceServiceAgreementUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        return new CompleteComplianceServiceAgreementUseCase(merchantRepository, eventPublisher);
    }
    
    @Bean
    public SubmitComplianceUseCase submitComplianceUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        return new SubmitComplianceUseCase(merchantRepository, eventPublisher);
    }
    
    // Customer Use Cases
    @Bean
    public CreateCustomerUseCase createCustomerUseCase(CustomerRepository customerRepository, DomainEventPublisher eventPublisher) {
        return new CreateCustomerUseCase(customerRepository, eventPublisher);
    }
    
    @Bean
    public GetCustomerUseCase getCustomerUseCase(CustomerQueryService queryService) {
        return new GetCustomerUseCase(queryService);
    }
    
    @Bean
    public ListCustomersUseCase listCustomersUseCase(CustomerQueryService queryService) {
        return new ListCustomersUseCase(queryService);
    }
    
    // SubAccount Use Cases
    @Bean
    public RegisterSubAccountUseCase registerSubAccountUseCase(
            SubAccountRepository subAccountRepository, AccountNameResolutionPort accountNameResolutionPort, DomainEventPublisher eventPublisher) {
        return new RegisterSubAccountUseCase(subAccountRepository, accountNameResolutionPort, eventPublisher);
    }
    
    @Bean
    public GetSubAccountUseCase getSubAccountUseCase(SubAccountQueryService queryService) {
        return new GetSubAccountUseCase(queryService);
    }
    
    @Bean
    public ListSubAccountsUseCase listSubAccountsUseCase(SubAccountQueryService queryService) {
        return new ListSubAccountsUseCase(queryService);
    }
    
    // API Key Use Cases
    @Bean
    public GenerateTestApiKeyPairUseCase generateTestApiKeyPairUseCase(
            ApiKeyRepository apiKeyRepository, PasswordEncoder passwordEncoder, DomainEventPublisher eventPublisher) {
        return new GenerateTestApiKeyPairUseCase(apiKeyRepository, passwordEncoder, eventPublisher);
    }
    
    @Bean
    public GenerateLiveApiKeyPairUseCase generateLiveApiKeyPairUseCase(
            MerchantRepository merchantRepository, ApiKeyRepository apiKeyRepository, PasswordEncoder passwordEncoder, DomainEventPublisher eventPublisher) {
        return new GenerateLiveApiKeyPairUseCase(merchantRepository, apiKeyRepository, passwordEncoder, eventPublisher);
    }
    
    @Bean
    public ListApiKeysUseCase listApiKeysUseCase(ApiKeyQueryService queryService) {
        return new ListApiKeysUseCase(queryService);
    }
    
    @Bean
    public RegenerateApiKeyUseCase regenerateApiKeyUseCase(
            MerchantRepository merchantRepository, ApiKeyRepository apiKeyRepository, PasswordEncoder passwordEncoder, DomainEventPublisher eventPublisher) {
        return new RegenerateApiKeyUseCase(merchantRepository, apiKeyRepository, passwordEncoder, eventPublisher);
    }
    
    @Bean
    public RevokeApiKeyUseCase revokeApiKeyUseCase(ApiKeyRepository apiKeyRepository, DomainEventPublisher eventPublisher) {
        return new RevokeApiKeyUseCase(apiKeyRepository, eventPublisher);
    }
}
