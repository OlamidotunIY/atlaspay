package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.dto.SubAccountDto;
import com.atlaspay.identity.application.port.out.SubAccountQueryService;
import com.atlaspay.identity.application.query.ListSubAccountsQuery;
import com.atlaspay.shared.usecase.BaseUseCase;
import com.atlaspay.shared.util.PageResult;

public class ListSubAccountsUseCase extends BaseUseCase<ListSubAccountsQuery, PageResult<SubAccountDto>> {

    private final SubAccountQueryService queryService;

    public ListSubAccountsUseCase(SubAccountQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public PageResult<SubAccountDto> execute(ListSubAccountsQuery query) {
        return queryService.findAllByMerchantId(
                query.merchantId(),
                query.page(),
                query.size()
        );
    }
}
