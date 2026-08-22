package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.CreateVerificationCommand;
import com.atlaspay.auth.application.port.out.OtpGeneratorPort;
import com.atlaspay.auth.application.port.out.PasswordEncoderPort;
import com.atlaspay.auth.domain.model.Verification;
import com.atlaspay.auth.domain.repository.VerificationRepository;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class CreateVerificationUseCase extends BaseUseCase<CreateVerificationCommand, ApiResponse<Void>> {

    private final VerificationRepository verificationRepository;
    private final OtpGeneratorPort otpGeneratorPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final DomainEventPublisher eventPublisher;
    
    @Value("${atlaspay.auth.verification.expires-in-minutes:10}")
    private int expiresInMinutes;

    @Value("${atlaspay.auth.verification.max-attempts:3}")
    private int maxAttempts;

    public CreateVerificationUseCase(
            VerificationRepository verificationRepository,
            OtpGeneratorPort otpGeneratorPort,
            PasswordEncoderPort passwordEncoderPort,
            DomainEventPublisher eventPublisher) {
        this.verificationRepository = verificationRepository;
        this.otpGeneratorPort = otpGeneratorPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<Void> execute(CreateVerificationCommand input) {
        verificationRepository.invalidatePreviousVerifications(input.type(), input.identifier());

        String rawCode = otpGeneratorPort.generateOtp();
        String hashedCode = passwordEncoderPort.encode(rawCode);

        Verification verification = Verification.create(
                verificationRepository.nextIdentity(),
                input.authAccountId(),
                input.identifier(),
                input.identifier(),
                hashedCode,
                input.type(),
                ZonedDateTime.now().plusMinutes(expiresInMinutes),
                maxAttempts
        );

        verificationRepository.save(verification);
        publishEvents(verification, eventPublisher);

        return new ApiResponse<>(true, "Verification created successfully", null, null);
    }
}
