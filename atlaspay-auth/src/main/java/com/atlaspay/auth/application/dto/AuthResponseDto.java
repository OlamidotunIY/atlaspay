package com.atlaspay.auth.application.dto;

public record AuthResponseDto(
        boolean requiresTwoFactor,
        String preAuthToken,
        AuthTokenDto tokens
) {
    public static AuthResponseDto forTwoFactor(String preAuthToken) {
        return new AuthResponseDto(true, preAuthToken, null);
    }
    
    public static AuthResponseDto forSuccess(AuthTokenDto tokens) {
        return new AuthResponseDto(false, null, tokens);
    }
}
