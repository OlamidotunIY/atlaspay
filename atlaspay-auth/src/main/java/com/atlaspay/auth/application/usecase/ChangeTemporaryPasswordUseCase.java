package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.command.ChangeTemporaryPasswordCommand;
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
public class ChangeTemporaryPasswordUseCase extends BaseUseCase<ChangeTemporaryPasswordCommand, ApiResponse<AuthResponseDto>> {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenGeneratorPort tokenGeneratorPort;
    private final PreAuthTokenStorePort preAuthTokenStorePort;
    private final TokenIssuanceService tokenIssuanceService;

    public ChangeTemporaryPasswordUseCase(
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
    public ApiResponse<AuthResponseDto> execute(ChangeTemporaryPasswordCommand input) {
        AuthAccount authAccount = authAccountRepository.findByIdentifier(input.identifier())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        if (authAccount.getStatus() != AuthStatus.REQUIRES_PASSWORD_CHANGE) {
            throw new BusinessRuleException(AuthErrorCode.INVALID_REQUEST, "Password change not required for this account");
        }

        if (!passwordEncoderPort.matches(input.oldPassword(), authAccount.getCredentialHash())) {
            throw new BusinessRuleException(AuthErrorCode.INVALID_CREDENTIAL, "Invalid credentials");
        }

        // Hash new password and update account
        String newHash = passwordEncoderPort.encode(input.newPassword());
        authAccount.updateCredential(newHash);
        authAccountRepository.save(authAccount);

        // Standard 2FA check after successful password change
        if (Boolean.TRUE.equals(authAccount.getTotpEnabled())) {
            TokenGeneratorPort.TokenData preAuth = tokenGeneratorPort.generatePreAuthToken(authAccount.getPrincipalId(), authAccount.getPrincipalType().name());
            preAuthTokenStorePort.store(preAuth.token(), authAccount.getId());
            return new ApiResponse<>(true, "Password changed. 2FA Required", AuthResponseDto.forTwoFactor(preAuth.token()), null);
        }

        // Issue tokens immediately if no 2FA required
        AuthResponseDto responseDto = AuthResponseDto.forSuccess(tokenIssuanceService.issueTokensAndCreateSession(authAccount, input.ipAddress(), input.userAgent()));
        return new ApiResponse<>(true, "Password changed successfully", responseDto, null);
    }
}
