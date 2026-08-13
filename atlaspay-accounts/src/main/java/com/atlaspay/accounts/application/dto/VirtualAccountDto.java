package com.atlaspay.accounts.application.dto;

public record VirtualAccountDto(Long id, Long integration, String customerCode, String accountName, String nuban, String bankName, String status) {
}
