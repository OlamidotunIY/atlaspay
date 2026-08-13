package com.atlaspay.identity.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlaspay.identity.application.command.VerifyMerchantEmailCommand;

import com.atlaspay.shared.usecase.BaseUseCase;

import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;

public class VerifyMerchantEmailUseCase extends BaseUseCase<VerifyMerchantEmailCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(VerifyMerchantEmailUseCase.class);


    private final MerchantRepository merchantRepository;
    private final DomainEventPublisher eventPublisher;

    public VerifyMerchantEmailUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void execute(VerifyMerchantEmailCommand command) {
        log.info("Executing VerifyMerchantEmailUseCase");

        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        merchant.verifyEmail(command.verificationCode());
        merchantRepository.save(merchant);

        publishEvents(merchant, eventPublisher);
    
        return null;
    }
}
