package com.atlaspay.identity.application.port;

import com.atlaspay.identity.application.dto.SubAccountDto;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.id.SubAccountId;
import com.atlaspay.shared.util.PageResult;

import java.util.Optional;

public interface SubAccountQueryService {
    Optional<SubAccountDto> findById(MerchantId merchantId, SubAccountId subAccountId);
    PageResult<SubAccountDto> findAllByMerchantId(MerchantId merchantId, int page, int size);
}
