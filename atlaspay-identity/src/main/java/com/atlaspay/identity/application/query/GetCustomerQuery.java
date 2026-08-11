package com.atlaspay.identity.application.query;

import com.atlaspay.shared.domain.id.CustomerId;
import com.atlaspay.shared.domain.id.MerchantId;

public record GetCustomerQuery(
    MerchantId merchantId,
    CustomerId customerId
) {}
