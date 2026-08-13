package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.dto.CustomerDto;
import com.atlaspay.identity.application.port.CustomerQueryService;
import com.atlaspay.identity.application.query.GetCustomerQuery;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;

public class GetCustomerUseCase extends BaseUseCase<GetCustomerQuery, CustomerDto> {

    private final CustomerQueryService queryService;

    public GetCustomerUseCase(CustomerQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public CustomerDto execute(GetCustomerQuery query) {
        return queryService.findById(query.merchantId(), query.customerId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
    }
}
