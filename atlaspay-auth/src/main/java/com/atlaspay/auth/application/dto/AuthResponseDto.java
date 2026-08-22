package com.atlaspay.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponseDto(
        boolean requiresTwoFactor,
        boolean requiresPasswordChange,
        String identifier,
        String preAuthToken,
        AuthTokenDto tokens
) {
    public static AuthResponseDto forTwoFactor(String preAuthToken) {
        return new AuthResponseDto(true, false, null, preAuthToken, null);
    }
    
    public static AuthResponseDto forPasswordChangeRequired(String identifier) {
        return new AuthResponseDto(false, true, identifier, null, null);
    }
    
    public static AuthResponseDto forSuccess(AuthTokenDto tokens) {
        return new AuthResponseDto(false, false, null, null, tokens);
    }
}
