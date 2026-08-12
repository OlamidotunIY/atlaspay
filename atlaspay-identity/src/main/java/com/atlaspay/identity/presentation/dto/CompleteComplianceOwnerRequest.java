package com.atlaspay.identity.presentation.dto;

import com.atlaspay.identity.domain.model.GovernmentIdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CompleteComplianceOwnerRequest(
    String bvn,
    String nin,
    
    @NotNull(message = "Date of birth is required")
    LocalDate dateOfBirth,
    
    @NotBlank(message = "Address is required")
    String address,
    
    @NotNull(message = "ID Type is required")
    GovernmentIdType idType,
    
    @NotBlank(message = "ID Number is required")
    String idNumber,
    
    String rcNumber
) {}
