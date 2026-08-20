package com.atlaspay.auth.application.dto;

public record PreAuthTokenDto(
        String preAuthToken,
        boolean requiresTwoFactor
) {}
