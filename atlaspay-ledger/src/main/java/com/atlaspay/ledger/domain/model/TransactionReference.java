package com.atlaspay.ledger.domain.model;

public record TransactionReference(
        String transactionId,
        SourceSystem sourceSystem
) {
    public TransactionReference {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be empty");
        }
        if (sourceSystem == null) {
            throw new IllegalArgumentException("sourceSystem must not be null");
        }
    }
}
