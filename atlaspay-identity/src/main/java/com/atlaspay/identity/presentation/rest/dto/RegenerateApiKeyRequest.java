package com.atlaspay.identity.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegenerateApiKeyRequest(
        @NotBlank(message = "Key type is required")
        @Pattern(regexp = "^(PUBLIC|SECRET)$", message = "Key type must be either PUBLIC or SECRET")
        String keyType,
        
        @NotBlank(message = "Environment is required")
        @Pattern(regexp = "^(TEST|LIVE)$", message = "Environment must be either TEST or LIVE")
        String environment
) {}
