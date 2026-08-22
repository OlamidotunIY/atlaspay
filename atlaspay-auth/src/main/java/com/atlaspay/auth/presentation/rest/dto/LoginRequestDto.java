package com.atlaspay.auth.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "Identifier is required")
    private String identifier;
    
    @NotBlank(message = "Password is required")
    private String password;
}
