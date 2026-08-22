package com.atlaspay.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlaspay.identity.application.dto.SubAccountDto;
import com.atlaspay.identity.application.port.SubAccountQueryService;
import com.atlaspay.identity.application.query.GetSubAccountQuery;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;

@Service
public class GetSubAccountUseCase extends BaseUseCase<GetSubAccountQuery, SubAccountDto> {
    private static final Logger log = LoggerFactory.getLogger(GetSubAccountUseCase.class);


    private final SubAccountQueryService queryService;

    public GetSubAccountUseCase(SubAccountQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public SubAccountDto execute(GetSubAccountQuery query) {
        log.info("Executing GetSubAccountUseCase");

        return queryService.findById(query.merchantId(), query.subAccountId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.SUBACCOUNT_NOT_FOUND, "SubAccount not found"));
    }
}


