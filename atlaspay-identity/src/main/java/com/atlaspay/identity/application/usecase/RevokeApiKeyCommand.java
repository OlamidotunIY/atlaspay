package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.domain.id.ApiKeyId;
import com.atlaspay.shared.domain.id.MerchantId;

public record RevokeApiKeyCommand(
    MerchantId authenticatedMerchantId,
    ApiKeyId keyId
) {}
