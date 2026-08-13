package com.atlaspay.identity.application.dto;


public record RegisterSubAccountResult(
    Long subAccountId,
    String accountName
) {}
