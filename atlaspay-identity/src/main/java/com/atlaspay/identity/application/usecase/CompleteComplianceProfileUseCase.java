package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.usecase.BaseCommandUseCase;

import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;

public class CompleteComplianceProfileUseCase extends BaseCommandUseCase<CompleteComplianceProfileCommand> {

    private final MerchantRepository merchantRepository;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceProfileUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void execute(CompleteComplianceProfileCommand command) {
        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        merchant.updateComplianceProfile(
            command.description(),
            command.staffSize(),
            command.industry(),
            command.category(),
            command.annualProjectedSalesVolume(),
            command.annualProjectedSalesCurrency()
        );

        merchantRepository.save(merchant);
        publishEvents(merchant, eventPublisher);
    }
}
