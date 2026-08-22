package com.atlaspay.admin.presentation.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAdminRequestDto(
    @NotBlank(message = "Full name is required")
    String fullName,
    
    @NotBlank(message = "Personal email is required")
    @Email(message = "Valid email is required")
    String personalEmail,
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(MASTER|SUPPORT|BILLING|CAREERS|GENERAL)$", message = "Invalid role")
    String role
) {}
