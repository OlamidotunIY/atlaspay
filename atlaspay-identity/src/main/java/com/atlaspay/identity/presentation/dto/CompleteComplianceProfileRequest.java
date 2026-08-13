package com.atlaspay.identity.presentation.dto;

import com.atlaspay.identity.domain.model.StaffSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CompleteComplianceProfileRequest(
    String description,
    
    @NotNull(message = "Staff size is required")
    StaffSize staffSize,
    
    @NotBlank(message = "Industry is required")
    String industry,
    
    @NotBlank(message = "Category is required")
    String category,
    
    AnnualProjectedSalesVolumeDto annualProjectedSalesVolume
) {}
