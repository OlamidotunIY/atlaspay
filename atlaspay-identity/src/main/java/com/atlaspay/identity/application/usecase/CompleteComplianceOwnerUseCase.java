package com.atlaspay.identity.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlaspay.identity.application.command.CompleteComplianceOwnerCommand;

import com.atlaspay.shared.usecase.BaseUseCase;

import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;

public class CompleteComplianceOwnerUseCase extends BaseUseCase<CompleteComplianceOwnerCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(CompleteComplianceOwnerUseCase.class);


    private final MerchantRepository merchantRepository;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceOwnerUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void execute(CompleteComplianceOwnerCommand command) {
        log.info("Executing CompleteComplianceOwnerUseCase");

        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        merchant.updateComplianceOwner(
            command.ownerBvn(),
            command.ownerNin(),
            command.ownerDateOfBirth(),
            command.ownerAddress(),
            command.ownerIdType(),
            command.ownerIdNumber(),
            command.rcNumber()
        );

        merchantRepository.save(merchant);
        publishEvents(merchant, eventPublisher);
    
        return null;
    }
}
