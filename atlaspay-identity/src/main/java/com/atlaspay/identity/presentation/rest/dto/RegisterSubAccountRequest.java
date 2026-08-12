package com.atlaspay.identity.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterSubAccountRequest(
        @NotBlank String bankCode,
        @NotBlank String accountNumber,
        String description
) {}
