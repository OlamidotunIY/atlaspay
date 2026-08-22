package com.atlaspay.auth.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record SetupPasswordRequestDto(
        @NotBlank(message = "Setup token is required")
        String setupToken,
        @NotBlank(message = "New password is required")
        String newPassword
) {}