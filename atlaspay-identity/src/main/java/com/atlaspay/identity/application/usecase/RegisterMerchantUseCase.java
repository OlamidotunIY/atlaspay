package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.usecase.BaseUseCase;

import com.atlaspay.identity.application.dto.ApiKeyPairResult;
import com.atlaspay.identity.application.dto.RegisterMerchantResult;
import com.atlaspay.identity.application.port.out.PasswordEncoder;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.ConflictException;

public class RegisterMerchantUseCase extends BaseUseCase<RegisterMerchantCommand, RegisterMerchantResult> {

    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final GenerateTestApiKeyPairUseCase generateTestApiKeyPairUseCase;
    private final DomainEventPublisher eventPublisher;

    public RegisterMerchantUseCase(
            MerchantRepository merchantRepository,
            PasswordEncoder passwordEncoder,
            GenerateTestApiKeyPairUseCase generateTestApiKeyPairUseCase,
            DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
        this.generateTestApiKeyPairUseCase = generateTestApiKeyPairUseCase;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RegisterMerchantResult execute(RegisterMerchantCommand command) {
        if (merchantRepository.findByEmail(command.email()).isPresent()) {
            throw new ConflictException(IdentityErrorCode.MERCHANT_EMAIL_ALREADY_EXISTS, "Merchant with this email already exists");
        }

        String hashedPassword = passwordEncoder.encode(command.password());

        Merchant merchant = new Merchant(
            MerchantId.generate(),
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

        publishEvents(merchant, eventPublisher);

        ApiKeyPairResult keys = generateTestApiKeyPairUseCase.execute(new GenerateTestApiKeyPairCommand(merchant.getId()));

        return new RegisterMerchantResult(merchant.getId(), keys.publicKey(), keys.secretKey());
    }
}
