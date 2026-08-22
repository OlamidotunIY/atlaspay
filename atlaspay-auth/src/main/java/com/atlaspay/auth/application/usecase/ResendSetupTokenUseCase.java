package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.ResendSetupTokenCommand;
import com.atlaspay.auth.application.port.out.SetupTokenStorePort;
import com.atlaspay.auth.application.port.out.TokenGeneratorPort;
import com.atlaspay.auth.domain.event.PasswordSetupInitiatedEvent;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.model.AuthStatus;
import com.atlaspay.auth.domain.repository.AuthAccountRepository;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class ResendSetupTokenUseCase extends BaseUseCase<ResendSetupTokenCommand, ApiResponse<Void>> {

    private final AuthAccountRepository authAccountRepository;
    private final SetupTokenStorePort setupTokenStorePort;
    private final TokenGeneratorPort tokenGeneratorPort;
    private final DomainEventPublisher eventPublisher;

    public ResendSetupTokenUseCase(
            AuthAccountRepository authAccountRepository,
            SetupTokenStorePort setupTokenStorePort,
            TokenGeneratorPort tokenGeneratorPort,
            DomainEventPublisher eventPublisher) {
        this.authAccountRepository = authAccountRepository;
        this.setupTokenStorePort = setupTokenStorePort;
        this.tokenGeneratorPort = tokenGeneratorPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ApiResponse<Void> execute(ResendSetupTokenCommand input) {
        AuthAccount authAccount = authAccountRepository.findByIdentifier(input.identifier())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        if (authAccount.getStatus() != AuthStatus.REQUIRES_PASSWORD_SETUP) {
            throw new BusinessRuleException(AuthErrorCode.INVALID_REQUEST, "Account is not in password setup phase");
        }

        TokenGeneratorPort.TokenData setupToken = tokenGeneratorPort.generateSetupToken(authAccount.getPrincipalId(), authAccount.getPrincipalType().name());
        setupTokenStorePort.store(setupToken.token(), authAccount.getId());

        PasswordSetupInitiatedEvent event = new PasswordSetupInitiatedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(authAccount.getId()),
                ZonedDateTime.now(),
                new PasswordSetupInitiatedEvent.Payload(authAccount.getIdentifier(), setupToken.token())
        );
        eventPublisher.publish(EnvelopedDomainEvent.wrap(event));

        return new ApiResponse<>(true, "Password setup token resent successfully", null, null);
    }
}