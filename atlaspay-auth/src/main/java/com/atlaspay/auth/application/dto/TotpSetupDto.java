package com.atlaspay.auth.application.dto;

public record TotpSetupDto(
        String secret,
        String qrCodeUri
) {}
