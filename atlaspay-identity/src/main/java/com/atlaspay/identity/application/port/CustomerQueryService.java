package com.atlaspay.identity.application.port;

import com.atlaspay.identity.application.dto.CustomerDto;
import com.atlaspay.shared.util.PageResult;

import java.util.Optional;

public interface CustomerQueryService {
    Optional<CustomerDto> findById(Long merchantId, Long customerId);
    PageResult<CustomerDto> findAllByMerchantId(Long merchantId, int page, int size, String emailFilter);
}
