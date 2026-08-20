package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.UpdateCredentialCommand;
import com.atlaspay.auth.application.port.out.PasswordEncoderPort;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.repository.AuthAccountRepository;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCredentialUseCase extends BaseUseCase<UpdateCredentialCommand, ApiResponse<Void>> {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final DomainEventPublisher eventPublisher;

    public UpdateCredentialUseCase(
            AuthAccountRepository authAccountRepository,
            PasswordEncoderPort passwordEncoderPort,
            DomainEventPublisher eventPublisher) {
        this.authAccountRepository = authAccountRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<Void> execute(UpdateCredentialCommand input) {
        AuthAccount authAccount = authAccountRepository.findById(input.authAccountId())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        String hash = passwordEncoderPort.encode(input.rawNewCredential());
        authAccount.updateCredential(hash);

        authAccountRepository.save(authAccount);
        publishEvents(authAccount, eventPublisher);

        return new ApiResponse<>(true, "Credential updated successfully", null, null);
    }
}
