package com.atlaspay.identity.presentation.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompleteComplianceContactRequest(
    @NotBlank(message = "Support email is required")
    @Email(message = "Support email must be valid")
    String supportEmail,
    
    @Email(message = "Dispute email must be valid")
    String disputeEmail,
    
    @NotBlank(message = "WhatsApp phone is required")
    String whatsappPhone,
    
    @NotBlank(message = "WhatsApp name is required")
    String whatsappName,
    
    String websiteUrl,
    String twitterHandle,
    String facebookUsername,
    String instagramHandle,
    
    @NotBlank(message = "Business state is required")
    String businessState,
    
    @NotBlank(message = "Business LGA is required")
    String businessLga,
    
    @NotBlank(message = "Business city is required")
    String businessCity,
    
    @NotBlank(message = "Business street is required")
    String businessStreet
) {}
