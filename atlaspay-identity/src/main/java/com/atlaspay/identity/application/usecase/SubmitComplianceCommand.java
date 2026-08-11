package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.domain.id.MerchantId;

public record SubmitComplianceCommand(
    MerchantId merchantId
) {}
