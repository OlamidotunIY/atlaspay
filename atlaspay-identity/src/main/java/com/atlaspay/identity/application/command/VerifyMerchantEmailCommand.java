package com.atlaspay.identity.application.command;


public record VerifyMerchantEmailCommand(
    Long merchantId,
    String verificationCode
) {}
