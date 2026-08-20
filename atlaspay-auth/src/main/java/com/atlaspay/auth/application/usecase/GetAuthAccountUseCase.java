package com.atlaspay.auth.application.usecase;

import com.atlaspay.auth.application.dto.AuthAccountDto;
import com.atlaspay.auth.application.query.GetAuthAccountQuery;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.repository.AuthAccountRepository;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAuthAccountUseCase extends BaseUseCase<GetAuthAccountQuery, ApiResponse<AuthAccountDto>> {

    private final AuthAccountRepository authAccountRepository;

    public GetAuthAccountUseCase(AuthAccountRepository authAccountRepository) {
        this.authAccountRepository = authAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AuthAccountDto> execute(GetAuthAccountQuery input) {
        AuthAccount account = authAccountRepository.findById(input.authAccountId())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        AuthAccountDto dto = new AuthAccountDto(
                account.getId(),
                account.getPrincipalId(),
                account.getPrincipalType(),
                account.getProvider(),
                account.getScope(),
                account.getTotpEnabled(),
                account.getStatus()
        );

        return new ApiResponse<>(true, "Auth account retrieved successfully", dto, null);
    }
}
