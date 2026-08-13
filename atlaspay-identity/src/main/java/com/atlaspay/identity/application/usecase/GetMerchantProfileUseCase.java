package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.dto.MerchantProfileDto;
import com.atlaspay.identity.application.port.MerchantQueryService;
import com.atlaspay.identity.application.query.GetMerchantProfileQuery;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;

public class GetMerchantProfileUseCase extends BaseUseCase<GetMerchantProfileQuery, MerchantProfileDto> {

    private final MerchantQueryService queryService;

    public GetMerchantProfileUseCase(MerchantQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public MerchantProfileDto execute(GetMerchantProfileQuery query) {
        return queryService.findProfileById(query.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));
    }
}
