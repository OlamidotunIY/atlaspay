package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.dto.SubAccountDto;
import com.atlaspay.identity.application.port.out.SubAccountQueryService;
import com.atlaspay.identity.application.query.GetSubAccountQuery;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;

public class GetSubAccountUseCase extends BaseUseCase<GetSubAccountQuery, SubAccountDto> {

    private final SubAccountQueryService queryService;

    public GetSubAccountUseCase(SubAccountQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public SubAccountDto execute(GetSubAccountQuery query) {
        return queryService.findById(query.merchantId(), query.subAccountId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.SUBACCOUNT_NOT_FOUND, "SubAccount not found"));
    }
}
