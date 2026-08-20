package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.CompleteVerificationCommand;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.model.Verification;
import com.atlaspay.auth.domain.repository.VerificationRepository;
import com.atlaspay.auth.domain.service.VerificationCodeHasher;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteVerificationUseCase extends BaseUseCase<CompleteVerificationCommand, ApiResponse<Void>> {

    private final VerificationRepository verificationRepository;
    private final VerificationCodeHasher verificationCodeHasher;
    private final DomainEventPublisher eventPublisher;

    public CompleteVerificationUseCase(
            VerificationRepository verificationRepository,
            VerificationCodeHasher verificationCodeHasher,
            DomainEventPublisher eventPublisher) {
        this.verificationRepository = verificationRepository;
        this.verificationCodeHasher = verificationCodeHasher;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<Void> execute(CompleteVerificationCommand input) {
        Verification verification = verificationRepository.findActiveByTypeAndValue(input.type(), input.identifier())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.VERIFICATION_EXPIRED, "Active verification not found"));

        verification.complete(input.code(), verificationCodeHasher);

        verificationRepository.save(verification);
        publishEvents(verification, eventPublisher);

        return new ApiResponse<>(true, "Verification completed successfully", null, null);
    }
}
