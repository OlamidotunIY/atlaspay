package com.atlaspay.identity.application.dto;

public record ApiKeyPairResult(
    String publicKey,
    String secretKey
) {}
