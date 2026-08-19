package com.atlaspay.ledger.application.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record LedgerHistoryDto(
    Long integration,
    String domain,
    BigDecimal balance,
    String currency,
    BigDecimal difference,
    String reason,
    String model_responsible,
    String model_row,
    Long id,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {}
