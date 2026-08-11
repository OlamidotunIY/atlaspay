package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.domain.id.MerchantId;

public record CompleteComplianceAccountCommand(
    MerchantId merchantId,
    String settlementBankCode,
    String settlementAccountNumber
) {}
