package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.AuthenticateCommand;
import com.atlaspay.auth.application.dto.AuthResponseDto;
import com.atlaspay.auth.application.port.out.PasswordEncoderPort;
import com.atlaspay.auth.application.port.out.PreAuthTokenStorePort;
import com.atlaspay.auth.application.port.out.TokenGeneratorPort;
import com.atlaspay.auth.application.service.TokenIssuanceService;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.model.AuthStatus;
import com.atlaspay.auth.domain.repository.AuthAccountRepository;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateUseCase extends BaseUseCase<AuthenticateCommand, ApiResponse<AuthResponseDto>> {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenGeneratorPort tokenGeneratorPort;
    private final PreAuthTokenStorePort preAuthTokenStorePort;
    private final TokenIssuanceService tokenIssuanceService;

    public AuthenticateUseCase(
            AuthAccountRepository authAccountRepository,
            PasswordEncoderPort passwordEncoderPort,
            TokenGeneratorPort tokenGeneratorPort,
            PreAuthTokenStorePort preAuthTokenStorePort,
            TokenIssuanceService tokenIssuanceService) {
        this.authAccountRepository = authAccountRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenGeneratorPort = tokenGeneratorPort;
        this.preAuthTokenStorePort = preAuthTokenStorePort;
        this.tokenIssuanceService = tokenIssuanceService;
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponseDto> execute(AuthenticateCommand input) {
        AuthAccount authAccount = authAccountRepository.findByPrincipalIdAndType(input.principalId(), input.principalType())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        if (authAccount.getStatus() == AuthStatus.LOCKED) {
            throw new BusinessRuleException(AuthErrorCode.ACCOUNT_LOCKED, "Account is locked");
        }
        if (authAccount.getStatus() == AuthStatus.SUSPENDED) {
            throw new BusinessRuleException(AuthErrorCode.ACCOUNT_SUSPENDED, "Account is suspended");
        }

        if (!passwordEncoderPort.matches(input.rawCredential(), authAccount.getCredentialHash())) {
            throw new BusinessRuleException(AuthErrorCode.INVALID_CREDENTIAL, "Invalid credentials");
        }

        if (Boolean.TRUE.equals(authAccount.getTotpEnabled())) {
            TokenGeneratorPort.TokenData preAuth = tokenGeneratorPort.generatePreAuthToken(authAccount.getPrincipalId(), authAccount.getPrincipalType().name());
            preAuthTokenStorePort.store(preAuth.token(), authAccount.getId());
            return new ApiResponse<>(true, "2FA Required", AuthResponseDto.forTwoFactor(preAuth.token()), null);
        }

        AuthResponseDto responseDto = AuthResponseDto.forSuccess(tokenIssuanceService.issueTokensAndCreateSession(authAccount, input.ipAddress(), input.userAgent()));
        return new ApiResponse<>(true, "Authentication successful", responseDto, null);
    }
}
