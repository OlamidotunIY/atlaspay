package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.shared.domain.id.MerchantId;

public record RegenerateApiKeyCommand(
    MerchantId authenticatedMerchantId,
    KeyType keyType,
    ApiEnvironment environment
) {}
