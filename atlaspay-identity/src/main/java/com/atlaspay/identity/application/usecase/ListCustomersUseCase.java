package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.dto.CustomerDto;
import com.atlaspay.identity.application.port.out.CustomerQueryService;
import com.atlaspay.identity.application.query.ListCustomersQuery;
import com.atlaspay.shared.usecase.BaseUseCase;
import com.atlaspay.shared.util.PageResult;

public class ListCustomersUseCase extends BaseUseCase<ListCustomersQuery, PageResult<CustomerDto>> {

    private final CustomerQueryService queryService;

    public ListCustomersUseCase(CustomerQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public PageResult<CustomerDto> execute(ListCustomersQuery query) {
        return queryService.findAllByMerchantId(
                query.merchantId(),
                query.page(),
                query.size(),
                query.emailFilter()
        );
    }
}
