package com.atlaspay.accounts.application.dto;

public record VirtualAccountDto(String id, String ownerId, String accountName, String nuban, String bankName, String status) {
}
