package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.CompleteTwoFactorCommand;
import com.atlaspay.auth.application.dto.AuthTokenDto;
import com.atlaspay.auth.application.port.out.PreAuthTokenStorePort;
import com.atlaspay.auth.application.port.out.TotpServicePort;
import com.atlaspay.auth.application.service.TokenIssuanceService;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.repository.AuthAccountRepository;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteTwoFactorUseCase extends BaseUseCase<CompleteTwoFactorCommand, ApiResponse<AuthTokenDto>> {

    private final AuthAccountRepository authAccountRepository;
    private final PreAuthTokenStorePort preAuthTokenStorePort;
    private final TotpServicePort totpServicePort;
    private final TokenIssuanceService tokenIssuanceService;

    public CompleteTwoFactorUseCase(
            AuthAccountRepository authAccountRepository,
            PreAuthTokenStorePort preAuthTokenStorePort,
            TotpServicePort totpServicePort,
            TokenIssuanceService tokenIssuanceService) {
        this.authAccountRepository = authAccountRepository;
        this.preAuthTokenStorePort = preAuthTokenStorePort;
        this.totpServicePort = totpServicePort;
        this.tokenIssuanceService = tokenIssuanceService;
    }

    @Override
    @Transactional
    public ApiResponse<AuthTokenDto> execute(CompleteTwoFactorCommand input) {
        Long authAccountId = preAuthTokenStorePort.consume(input.preAuthToken())
                .orElseThrow(() -> new BusinessRuleException(AuthErrorCode.INVALID_PRE_AUTH_TOKEN, "Invalid or expired pre-auth token"));

        AuthAccount authAccount = authAccountRepository.findById(authAccountId)
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        if (!totpServicePort.verify(authAccount.getTotpSecret(), input.code())) {
            throw new BusinessRuleException(AuthErrorCode.INVALID_TOTP_CODE, "Invalid TOTP code");
        }

        AuthTokenDto tokenDto = tokenIssuanceService.issueTokensAndCreateSession(authAccount, input.ipAddress(), input.userAgent());
        return new ApiResponse<>(true, "2FA authentication successful", tokenDto, null);
    }
}
