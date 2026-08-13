package com.atlaspay.identity.application.command;

import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.KeyType;

public record RegenerateApiKeyCommand(
    Long authenticatedMerchantId,
    KeyType keyType,
    ApiEnvironment environment
) {}
