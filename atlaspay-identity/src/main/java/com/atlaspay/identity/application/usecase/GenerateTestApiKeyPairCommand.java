package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.domain.id.MerchantId;

public record GenerateTestApiKeyPairCommand(MerchantId merchantId) {}
