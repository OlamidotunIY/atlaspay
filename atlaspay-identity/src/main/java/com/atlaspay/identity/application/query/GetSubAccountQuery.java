package com.atlaspay.identity.application.query;

import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.id.SubAccountId;

public record GetSubAccountQuery(
    MerchantId merchantId,
    SubAccountId subAccountId
) {}
