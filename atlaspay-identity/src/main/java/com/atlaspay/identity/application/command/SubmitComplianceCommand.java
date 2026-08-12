package com.atlaspay.identity.application.command;

import com.atlaspay.shared.domain.id.MerchantId;

public record SubmitComplianceCommand(
    MerchantId merchantId
) {}
