package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.event.DomainEvent;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.NotFoundException;

public class CompleteComplianceServiceAgreementUseCase {

    private final MerchantRepository merchantRepository;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceServiceAgreementUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(CompleteComplianceServiceAgreementCommand command) {
        if (!command.agreed()) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_NOT_ALL_STEPS_COMPLETE, "Must agree to service agreement");
        }

        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        merchant.acceptServiceAgreement();

        merchantRepository.save(merchant);
        merchant.pullDomainEvents().forEach(this::publishEvent);
    }

    private <T> void publishEvent(DomainEvent<T> event) {
        eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
    }
}
