package com.atlaspay.identity.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlaspay.identity.application.dto.ApiKeyDto;
import com.atlaspay.identity.application.port.ApiKeyQueryService;
import com.atlaspay.identity.application.query.ListApiKeysQuery;
import com.atlaspay.shared.usecase.BaseUseCase;

import java.util.List;

public class ListApiKeysUseCase extends BaseUseCase<ListApiKeysQuery, List<ApiKeyDto>> {
    private static final Logger log = LoggerFactory.getLogger(ListApiKeysUseCase.class);


    private final ApiKeyQueryService queryService;

    public ListApiKeysUseCase(ApiKeyQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public List<ApiKeyDto> execute(ListApiKeysQuery query) {
        return queryService.findAllByIntegration(query.merchantId());
    }
}
