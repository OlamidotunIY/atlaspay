package com.atlaspay.accounts.application.dto;

public record AccountIssuanceRequestDto(
        String referenceId,
        String accountName,
        String bankName
) {
}
