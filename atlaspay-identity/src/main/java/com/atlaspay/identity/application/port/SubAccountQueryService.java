package com.atlaspay.identity.application.port;

import com.atlaspay.identity.application.dto.SubAccountDto;
import com.atlaspay.shared.util.PageResult;

import java.util.Optional;

public interface SubAccountQueryService {
    Optional<SubAccountDto> findById(Long merchantId, Long subAccountId);
    PageResult<SubAccountDto> findAllByMerchantId(Long merchantId, int page, int size);
}
