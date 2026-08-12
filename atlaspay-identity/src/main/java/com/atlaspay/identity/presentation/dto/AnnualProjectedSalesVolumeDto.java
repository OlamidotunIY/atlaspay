package com.atlaspay.identity.presentation.dto;

import java.math.BigDecimal;

public record AnnualProjectedSalesVolumeDto(
    BigDecimal amount,
    String currency
) {}
