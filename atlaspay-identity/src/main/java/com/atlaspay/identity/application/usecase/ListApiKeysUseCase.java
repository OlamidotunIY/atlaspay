package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.dto.ApiKeyDto;
import com.atlaspay.identity.application.port.ApiKeyQueryService;
import com.atlaspay.identity.application.query.ListApiKeysQuery;
import com.atlaspay.shared.usecase.BaseUseCase;

import java.util.List;

public class ListApiKeysUseCase extends BaseUseCase<ListApiKeysQuery, List<ApiKeyDto>> {

    private final ApiKeyQueryService queryService;

    public ListApiKeysUseCase(ApiKeyQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public List<ApiKeyDto> execute(ListApiKeysQuery query) {
        return queryService.findAllByMerchantId(query.merchantId());
    }
}
