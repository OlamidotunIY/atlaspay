package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.CompleteVerificationCommand;
import com.atlaspay.auth.application.dto.VerificationResponseDto;
import com.atlaspay.auth.application.port.out.SetupTokenStorePort;
import com.atlaspay.auth.application.port.out.TokenGeneratorPort;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.model.AuthStatus;
import com.atlaspay.auth.domain.model.Verification;
import com.atlaspay.auth.domain.repository.AuthAccountRepository;
import com.atlaspay.auth.domain.repository.VerificationRepository;
import com.atlaspay.auth.domain.service.VerificationCodeHasher;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteVerificationUseCase extends BaseUseCase<CompleteVerificationCommand, ApiResponse<VerificationResponseDto>> {

    private final VerificationRepository verificationRepository;
    private final AuthAccountRepository authAccountRepository;
    private final VerificationCodeHasher verificationCodeHasher;
    private final DomainEventPublisher eventPublisher;
    private final SetupTokenStorePort setupTokenStorePort;
    private final TokenGeneratorPort tokenGeneratorPort;

    public CompleteVerificationUseCase(
            VerificationRepository verificationRepository,
            AuthAccountRepository authAccountRepository,
            VerificationCodeHasher verificationCodeHasher,
            DomainEventPublisher eventPublisher,
            SetupTokenStorePort setupTokenStorePort,
            TokenGeneratorPort tokenGeneratorPort) {
        this.verificationRepository = verificationRepository;
        this.authAccountRepository = authAccountRepository;
        this.verificationCodeHasher = verificationCodeHasher;
        this.eventPublisher = eventPublisher;
        this.setupTokenStorePort = setupTokenStorePort;
        this.tokenGeneratorPort = tokenGeneratorPort;
    }

    @Override
    @Transactional
    public ApiResponse<VerificationResponseDto> execute(CompleteVerificationCommand input) {
        Verification verification = verificationRepository.findActiveByTypeAndValue(input.type(), input.identifier())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.VERIFICATION_EXPIRED, "Active verification not found"));

        verification.complete(input.code(), verificationCodeHasher);
        verificationRepository.save(verification);
        publishEvents(verification, eventPublisher);

        AuthAccount authAccount = authAccountRepository.findById(verification.getAuthAccountId())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        if (authAccount.getStatus() == AuthStatus.PENDING_EMAIL_VERIFICATION) {
            authAccount.requirePasswordSetup();
            authAccountRepository.save(authAccount);

            TokenGeneratorPort.TokenData setupToken = tokenGeneratorPort.generateSetupToken(authAccount.getPrincipalId(), authAccount.getPrincipalType().name());
            setupTokenStorePort.store(setupToken.token(), authAccount.getId());

            VerificationResponseDto responseDto = VerificationResponseDto.requiresPasswordSetup(setupToken.token());
            return new ApiResponse<>(true, "Verification completed. Password setup required.", responseDto, null);
        }

        return new ApiResponse<>(true, "Verification completed successfully", VerificationResponseDto.completed(), null);
    }
}
