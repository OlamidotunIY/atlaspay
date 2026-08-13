package com.atlaspay.identity.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlaspay.identity.application.dto.CustomerDto;
import com.atlaspay.identity.application.port.CustomerQueryService;
import com.atlaspay.identity.application.query.ListCustomersQuery;
import com.atlaspay.shared.usecase.BaseUseCase;
import com.atlaspay.shared.util.PageResult;

public class ListCustomersUseCase extends BaseUseCase<ListCustomersQuery, PageResult<CustomerDto>> {
    private static final Logger log = LoggerFactory.getLogger(ListCustomersUseCase.class);


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
