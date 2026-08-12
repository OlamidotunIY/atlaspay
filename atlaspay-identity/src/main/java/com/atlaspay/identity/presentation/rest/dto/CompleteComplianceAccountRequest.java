package com.atlaspay.identity.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CompleteComplianceAccountRequest(
    @NotBlank(message = "Bank code is required")
    String bankCode,
    
    @NotBlank(message = "Account number is required")
    String accountNumber
) {}
