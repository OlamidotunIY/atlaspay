package com.atlaspay.identity.application.query;

import com.atlaspay.shared.domain.id.MerchantId;

public record ListSubAccountsQuery(
    MerchantId merchantId,
    int page,
    int size
) {}
