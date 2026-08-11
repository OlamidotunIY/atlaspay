package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import com.atlaspay.shared.event.DomainEvent;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
import com.atlaspay.shared.exception.NotFoundException;

public class CompleteComplianceContactUseCase {

    private final MerchantRepository merchantRepository;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceContactUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(CompleteComplianceContactCommand command) {
        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        merchant.updateComplianceContact(
            command.supportEmail() != null ? new EmailAddress(command.supportEmail()) : null,
            command.disputeEmail() != null ? new EmailAddress(command.disputeEmail()) : null,
            command.whatsappPhone() != null ? new PhoneNumber(command.whatsappPhone()) : null,
            command.whatsappName(),
            command.websiteUrl(),
            command.twitterHandle(),
            command.facebookUsername(),
            command.instagramHandle(),
            command.businessState(),
            command.businessLga(),
            command.businessCity(),
            command.businessStreet()
        );

        merchantRepository.save(merchant);
        merchant.pullDomainEvents().forEach(this::publishEvent);
    }

    private <T> void publishEvent(DomainEvent<T> event) {
        eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
    }
}
