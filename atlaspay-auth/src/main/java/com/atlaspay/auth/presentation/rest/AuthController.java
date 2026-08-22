package com.atlaspay.auth.presentation.rest;

import com.atlaspay.auth.application.command.AuthenticateCommand;
import com.atlaspay.auth.application.command.ChangeTemporaryPasswordCommand;
import com.atlaspay.auth.application.command.CompleteTwoFactorCommand;
import com.atlaspay.auth.application.command.CompleteVerificationCommand;
import com.atlaspay.auth.application.command.RefreshTokenCommand;
import com.atlaspay.auth.application.command.RevokeSessionCommand;
import com.atlaspay.auth.application.command.SetupPasswordCommand;
import com.atlaspay.auth.application.dto.AuthResponseDto;
import com.atlaspay.auth.application.dto.AuthTokenDto;
import com.atlaspay.auth.application.dto.VerificationResponseDto;
import com.atlaspay.auth.application.usecase.AuthenticateUseCase;
import com.atlaspay.auth.application.usecase.ChangeTemporaryPasswordUseCase;
import com.atlaspay.auth.application.usecase.CompleteTwoFactorUseCase;
import com.atlaspay.auth.application.usecase.CompleteVerificationUseCase;
import com.atlaspay.auth.application.usecase.RefreshTokenUseCase;
import com.atlaspay.auth.application.usecase.RevokeSessionUseCase;
import com.atlaspay.auth.application.usecase.SetupPasswordUseCase;
import com.atlaspay.auth.application.usecase.ResendSetupTokenUseCase;
import com.atlaspay.auth.presentation.rest.dto.ResendSetupTokenRequestDto;
import com.atlaspay.auth.presentation.rest.dto.ChangeTemporaryPasswordRequestDto;
import com.atlaspay.auth.presentation.rest.dto.CompleteVerificationRequestDto;
import com.atlaspay.auth.presentation.rest.dto.LoginRequestDto;
import com.atlaspay.auth.presentation.rest.dto.SetupPasswordRequestDto;
import com.atlaspay.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticateUseCase authenticateUseCase;
    private final ChangeTemporaryPasswordUseCase changeTemporaryPasswordUseCase;
    private final CompleteTwoFactorUseCase completeTwoFactorUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RevokeSessionUseCase revokeSessionUseCase;
    private final CompleteVerificationUseCase completeVerificationUseCase;
    private final SetupPasswordUseCase setupPasswordUseCase;
    private final ResendSetupTokenUseCase resendSetupTokenUseCase;

    @PostMapping("/setup-password/resend")
    public ResponseEntity<ApiResponse<Void>> resendSetupToken(
            @Valid @RequestBody ResendSetupTokenRequestDto request) {
        
        com.atlaspay.auth.application.command.ResendSetupTokenCommand command = 
            new com.atlaspay.auth.application.command.ResendSetupTokenCommand(request.identifier());
        
        return ResponseEntity.ok(resendSetupTokenUseCase.execute(command));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest) {
        
        AuthenticateCommand command = new AuthenticateCommand(
                request.getIdentifier(),
                request.getPassword(),
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );
        
        return ResponseEntity.ok(authenticateUseCase.execute(command));
    }

    @PostMapping("/password/change")
    public ResponseEntity<ApiResponse<AuthResponseDto>> changeTemporaryPassword(
            @Valid @RequestBody ChangeTemporaryPasswordRequestDto request,
            HttpServletRequest httpRequest) {
        
        ChangeTemporaryPasswordCommand command = new ChangeTemporaryPasswordCommand(
                request.getIdentifier(),
                request.getOldPassword(),
                request.getNewPassword(),
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );
        
        return ResponseEntity.ok(changeTemporaryPasswordUseCase.execute(command));
    }
    
    @PostMapping("/setup-password")
    public ResponseEntity<ApiResponse<AuthResponseDto>> setupPassword(
            @Valid @RequestBody SetupPasswordRequestDto request,
            HttpServletRequest httpRequest) {
        
        SetupPasswordCommand command = new SetupPasswordCommand(
                request.setupToken(),
                request.newPassword(),
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );
        
        return ResponseEntity.ok(setupPasswordUseCase.execute(command));
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerificationResponseDto>> verifyEmail(
            @Valid @RequestBody CompleteVerificationRequestDto request) {
        
        CompleteVerificationCommand command = new CompleteVerificationCommand(
                request.type(),
                request.identifier(),
                request.code()
        );
        
        return ResponseEntity.ok(completeVerificationUseCase.execute(command));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<ApiResponse<AuthTokenDto>> verifyMfa(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        CompleteTwoFactorCommand command = new CompleteTwoFactorCommand(
                request.get("preAuthToken"),
                request.get("code"),
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );
        
        return ResponseEntity.ok(completeTwoFactorUseCase.execute(command));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokenDto>> refreshToken(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        RefreshTokenCommand command = new RefreshTokenCommand(
                request.get("refreshToken"),
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );
        
        return ResponseEntity.ok(refreshTokenUseCase.execute(command));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody Map<String, String> request) {
        
        RevokeSessionCommand command = new RevokeSessionCommand(
                request.get("jti")
        );
        
        return ResponseEntity.ok(revokeSessionUseCase.execute(command));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
