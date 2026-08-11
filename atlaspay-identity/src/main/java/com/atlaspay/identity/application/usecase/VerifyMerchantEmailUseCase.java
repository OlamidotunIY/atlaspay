package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.usecase.BaseCommandUseCase;

import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;

public class VerifyMerchantEmailUseCase extends BaseCommandUseCase<VerifyMerchantEmailCommand> {

    private final MerchantRepository merchantRepository;
    private final DomainEventPublisher eventPublisher;

    public VerifyMerchantEmailUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void execute(VerifyMerchantEmailCommand command) {
        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        merchant.verifyEmail(command.verificationCode());
        merchantRepository.save(merchant);

        publishEvents(merchant, eventPublisher);
    }
}
