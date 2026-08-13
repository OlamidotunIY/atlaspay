package com.atlaspay.identity.application.command;


public record RevokeApiKeyCommand(
    Long authenticatedMerchantId,
    Long keyId
) {}
