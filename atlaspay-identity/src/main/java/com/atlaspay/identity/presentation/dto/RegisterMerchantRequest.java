package com.atlaspay.identity.presentation.dto;

import com.atlaspay.identity.domain.model.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterMerchantRequest(
    @NotBlank(message = "Country is required")
    String country,
    
    @NotBlank(message = "Business name is required")
    String businessName,
    
    @NotBlank(message = "First name is required")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    String lastName,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,
    
    @NotBlank(message = "Phone number is required")
    String phone,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password,
    
    @NotNull(message = "Business type is required")
    BusinessType businessType
) {}
