package com.atlaspay.simulator.application.dto;

public record AccountGenerationRequestDto(
    String referenceId,
    String accountName,
    String callbackUrl,
    String bankName
) {}
