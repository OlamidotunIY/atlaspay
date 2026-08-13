package com.atlaspay.identity.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlaspay.identity.application.dto.CustomerDto;
import com.atlaspay.identity.application.port.CustomerQueryService;
import com.atlaspay.identity.application.query.GetCustomerQuery;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;

public class GetCustomerUseCase extends BaseUseCase<GetCustomerQuery, CustomerDto> {
    private static final Logger log = LoggerFactory.getLogger(GetCustomerUseCase.class);


    private final CustomerQueryService queryService;

    public GetCustomerUseCase(CustomerQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public CustomerDto execute(GetCustomerQuery query) {
        log.info("Executing GetCustomerUseCase");

        return queryService.findById(query.merchantId(), query.customerId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
    }
}
