package com.atlaspay.admin.presentation.rest;

import com.atlaspay.admin.application.command.CreateAdminCommand;
import com.atlaspay.admin.application.command.GenerateCompanyEmailAliasCommand;
import com.atlaspay.admin.application.dto.CreateAdminResult;
import com.atlaspay.admin.application.dto.GenerateEmailResult;
import com.atlaspay.admin.application.usecase.CreateAdminUseCase;
import com.atlaspay.admin.application.usecase.GenerateCompanyEmailAliasUseCase;
import com.atlaspay.admin.presentation.rest.dto.CreateAdminRequestDto;
import com.atlaspay.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admins")
public class AdminController {

    private final CreateAdminUseCase createAdminUseCase;
    private final GenerateCompanyEmailAliasUseCase generateCompanyEmailAliasUseCase;

    public AdminController(CreateAdminUseCase createAdminUseCase, GenerateCompanyEmailAliasUseCase generateCompanyEmailAliasUseCase) {
        this.createAdminUseCase = createAdminUseCase;
        this.generateCompanyEmailAliasUseCase = generateCompanyEmailAliasUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateAdminResult>> createAdmin(@Valid @RequestBody CreateAdminRequestDto request) {
        // In a real app, we would verify the requester is a MASTER admin via Spring Security principal
        
        CreateAdminCommand command = new CreateAdminCommand(
            request.fullName(),
            request.personalEmail(),
            request.role()
        );
        
        CreateAdminResult result = createAdminUseCase.execute(command);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Admin created successfully", result, null));
    }

    @PostMapping("/me/generate-email")
    public ResponseEntity<ApiResponse<GenerateEmailResult>> generateCompanyEmail(
            @RequestHeader("X-Admin-Id") Long adminId) {
        // We use X-Admin-Id header to simulate the currently authenticated admin ID.
        // In production, extract this from the SecurityContextHolder (JWT token).
        
        GenerateCompanyEmailAliasCommand command = new GenerateCompanyEmailAliasCommand(adminId);
        GenerateEmailResult result = generateCompanyEmailAliasUseCase.execute(command);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Company email generated successfully", result, null));
    }
}

