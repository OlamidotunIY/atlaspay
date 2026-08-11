package com.atlaspay.identity.application.port.out;

import com.atlaspay.identity.application.dto.CustomerDto;
import com.atlaspay.shared.domain.id.CustomerId;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.util.PageResult;

import java.util.Optional;

public interface CustomerQueryService {
    Optional<CustomerDto> findById(MerchantId merchantId, CustomerId customerId);
    PageResult<CustomerDto> findAllByMerchantId(MerchantId merchantId, int page, int size, String emailFilter);
}
