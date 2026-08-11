package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.domain.id.MerchantId;

public record RegisterSubAccountCommand(
    MerchantId merchantId,
    String bankCode,
    String accountNumber,
    String description
) {}
