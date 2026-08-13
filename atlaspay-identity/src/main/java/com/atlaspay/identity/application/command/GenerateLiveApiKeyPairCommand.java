package com.atlaspay.identity.application.command;


public record GenerateLiveApiKeyPairCommand(
    Long merchantId
) {}
