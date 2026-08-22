package com.atlaspay.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerificationResponseDto(
        String nextAction,
        String sessionToken
) {
    public static VerificationResponseDto requiresPasswordSetup(String sessionToken) {
        return new VerificationResponseDto("SETUP_PASSWORD", sessionToken);
    }

    public static VerificationResponseDto completed() {
        return new VerificationResponseDto(null, null);
    }
}