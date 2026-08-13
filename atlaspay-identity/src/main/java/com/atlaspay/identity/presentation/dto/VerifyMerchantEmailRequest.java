package com.atlaspay.identity.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyMerchantEmailRequest(
    @NotBlank(message = "Verification code is required")
    String code,
    
    @NotBlank(message = "Merchant ID is required")
    String merchantId
) {}
