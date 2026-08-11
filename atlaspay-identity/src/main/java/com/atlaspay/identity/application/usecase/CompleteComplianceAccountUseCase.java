package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.port.out.AccountResolutionService;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.event.DomainEvent;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
import com.atlaspay.shared.exception.NotFoundException;

public class CompleteComplianceAccountUseCase {

    private final MerchantRepository merchantRepository;
    private final AccountResolutionService accountResolutionService;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceAccountUseCase(
            MerchantRepository merchantRepository,
            AccountResolutionService accountResolutionService,
            DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.accountResolutionService = accountResolutionService;
        this.eventPublisher = eventPublisher;
    }

    public void execute(CompleteComplianceAccountCommand command) {
        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        String accountName = accountResolutionService.resolveAccountName(command.settlementBankCode(), command.settlementAccountNumber())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.BANK_ACCOUNT_NOT_FOUND, "Bank account could not be resolved"));

        merchant.updateComplianceAccount(
            command.settlementBankCode(),
            command.settlementAccountNumber(),
            accountName
        );

        merchantRepository.save(merchant);
        merchant.pullDomainEvents().forEach(this::publishEvent);
    }

    private <T> void publishEvent(DomainEvent<T> event) {
        eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
    }
}
