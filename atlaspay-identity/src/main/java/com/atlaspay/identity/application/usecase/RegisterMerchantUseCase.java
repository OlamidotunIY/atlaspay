package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.command.RegisterMerchantCommand;
import com.atlaspay.shared.usecase.BaseUseCase;
import com.atlaspay.identity.application.dto.RegisterMerchantResult;
import com.atlaspay.identity.application.port.PasswordEncoder;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class RegisterMerchantUseCase extends BaseUseCase<RegisterMerchantCommand, RegisterMerchantResult> {

    private static final Logger log = LoggerFactory.getLogger(RegisterMerchantUseCase.class);

    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher eventPublisher;

    public RegisterMerchantUseCase(
            MerchantRepository merchantRepository,
            PasswordEncoder passwordEncoder,
            DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public RegisterMerchantResult execute(RegisterMerchantCommand command) {
        log.info("Starting merchant registration for email: {}", command.email());

        if (merchantRepository.findByEmail(command.email()).isPresent()) {
            log.warn("Merchant registration failed: email {} already exists", command.email());
            throw new ConflictException(IdentityErrorCode.MERCHANT_EMAIL_ALREADY_EXISTS, "Merchant with this email already exists");
        }

        String hashedPassword = passwordEncoder.encode(command.password());

        Merchant merchant = new Merchant(merchantRepository.nextIdentity(),
            command.country(),
            command.businessName(),
            command.firstName(),
            command.lastName(),
            new EmailAddress(command.email()),
            new PhoneNumber(command.phone()),
            hashedPassword,
            command.businessType()
        );

        merchantRepository.save(merchant);
        log.debug("Merchant saved with ID: {}", merchant.getId());

        publishEvents(merchant, eventPublisher);
        log.debug("Merchant domain events published");

        log.info("Successfully registered merchant with ID: {}", merchant.getId());

        return new RegisterMerchantResult(merchant.getId());
    }
}
