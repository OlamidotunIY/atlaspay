package com.atlaspay.identity.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterSubAccountRequest(
        @NotBlank String bankCode,
        @NotBlank String accountNumber,
        String description
) {}
