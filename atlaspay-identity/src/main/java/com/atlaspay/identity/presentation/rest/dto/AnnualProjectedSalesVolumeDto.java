package com.atlaspay.identity.presentation.rest.dto;

import java.math.BigDecimal;

public record AnnualProjectedSalesVolumeDto(
    BigDecimal amount,
    String currency
) {}
