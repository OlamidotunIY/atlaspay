package com.atlaspay.identity.application.command;

import com.atlaspay.shared.domain.id.MerchantId;

public record CompleteComplianceServiceAgreementCommand(
    MerchantId merchantId,
    boolean agreed
) {}
