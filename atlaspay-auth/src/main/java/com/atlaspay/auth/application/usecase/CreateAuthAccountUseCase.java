package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.CreateAuthAccountCommand;
import com.atlaspay.auth.application.port.out.PasswordEncoderPort;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.repository.AuthAccountRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.ConflictException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAuthAccountUseCase extends BaseUseCase<CreateAuthAccountCommand, Void> {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final DomainEventPublisher eventPublisher;

    public CreateAuthAccountUseCase(
            AuthAccountRepository authAccountRepository,
            PasswordEncoderPort passwordEncoderPort,
            DomainEventPublisher eventPublisher) {
        this.authAccountRepository = authAccountRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void execute(CreateAuthAccountCommand input) {
        if (authAccountRepository.existsByPrincipalIdAndType(input.principalId(), input.principalType())) {
            throw new ConflictException(AuthErrorCode.AUTH_ACCOUNT_ALREADY_EXISTS, "Auth account already exists for this principal");
        }

        String hash = null;
        if (input.rawCredential() != null && !input.rawCredential().isBlank()) {
            hash = passwordEncoderPort.encode(input.rawCredential());
        }

        AuthAccount authAccount = AuthAccount.create(
                authAccountRepository.nextIdentity(),
                input.principalId(),
                input.principalType(),
                input.provider(),
                hash,
                input.scope()
        );

        authAccountRepository.save(authAccount);
        publishEvents(authAccount, eventPublisher);

        return null;
    }
}
