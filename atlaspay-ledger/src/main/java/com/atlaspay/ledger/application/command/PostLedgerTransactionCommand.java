package com.atlaspay.ledger.application.command;

import com.atlaspay.ledger.domain.model.EntryType;
import com.atlaspay.shared.money.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;

public record PostLedgerTransactionCommand(
        String transactionId,
        String sourceSystem,
        List<EntryCommand> entries
) {
    public record EntryCommand(
            Long accountId,
            BigDecimal amount,
            CurrencyCode currency,
            EntryType type,
            String description
    ) {}
}
