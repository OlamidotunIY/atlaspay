package com.atlaspay.ledger.domain.model;

public record TransactionReference(
        String transactionId,
        String sourceSystem
) {
    public TransactionReference {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or blank");
        }
        if (sourceSystem == null || sourceSystem.isBlank()) {
            throw new IllegalArgumentException("Source system cannot be null or blank");
        }
    }
}
