package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.EnableTotpCommand;
import com.atlaspay.auth.application.dto.TotpSetupDto;
import com.atlaspay.auth.application.port.out.TotpServicePort;
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
public class EnableTotpUseCase extends BaseUseCase<EnableTotpCommand, ApiResponse<TotpSetupDto>> {

    private final AuthAccountRepository authAccountRepository;
    private final TotpServicePort totpServicePort;
    private final DomainEventPublisher eventPublisher;

    public EnableTotpUseCase(
            AuthAccountRepository authAccountRepository,
            TotpServicePort totpServicePort,
            DomainEventPublisher eventPublisher) {
        this.authAccountRepository = authAccountRepository;
        this.totpServicePort = totpServicePort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<TotpSetupDto> execute(EnableTotpCommand input) {
        AuthAccount authAccount = authAccountRepository.findById(input.authAccountId())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        String secret = totpServicePort.generateSecret();
        authAccount.enableTotp(secret);

        authAccountRepository.save(authAccount);
        publishEvents(authAccount, eventPublisher);

        String accountName = authAccount.getPrincipalType().name() + "-" + authAccount.getPrincipalId();
        String qrCodeUri = totpServicePort.generateUri(secret, accountName);

        TotpSetupDto dto = new TotpSetupDto(secret, qrCodeUri);
        return new ApiResponse<>(true, "TOTP enabled successfully", dto, null);
    }
}
