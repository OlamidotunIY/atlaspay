package com.atlaspay.auth.presentation.rest.dto;

import com.atlaspay.auth.domain.model.VerificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompleteVerificationRequestDto(
        @NotBlank(message = "Identifier is required")
        String identifier,
        @NotBlank(message = "Code is required")
        String code,
        @NotNull(message = "Type is required")
        VerificationType type
) {}