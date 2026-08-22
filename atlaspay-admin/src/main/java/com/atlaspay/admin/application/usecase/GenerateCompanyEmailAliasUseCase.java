package com.atlaspay.admin.application.usecase;

import com.atlaspay.admin.application.command.GenerateCompanyEmailAliasCommand;
import com.atlaspay.admin.application.dto.GenerateEmailResult;
import com.atlaspay.admin.application.port.out.CloudflareEmailPort;
import com.atlaspay.admin.domain.exception.AdminErrorCode;
import com.atlaspay.admin.domain.model.Admin;
import com.atlaspay.admin.domain.model.AdminRole;
import com.atlaspay.admin.domain.repository.AdminRepository;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.ConflictException;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenerateCompanyEmailAliasUseCase extends BaseUseCase<GenerateCompanyEmailAliasCommand, GenerateEmailResult> {

    private final AdminRepository adminRepository;
    private final CloudflareEmailPort cloudflareEmailPort;
    private final DomainEventPublisher eventPublisher;

    public GenerateCompanyEmailAliasUseCase(
            AdminRepository adminRepository, 
            CloudflareEmailPort cloudflareEmailPort, 
            DomainEventPublisher eventPublisher) {
        this.adminRepository = adminRepository;
        this.cloudflareEmailPort = cloudflareEmailPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public GenerateEmailResult execute(GenerateCompanyEmailAliasCommand command) {
        Admin admin = adminRepository.findById(command.adminId())
                .orElseThrow(() -> new NotFoundException(AdminErrorCode.ADMIN_NOT_FOUND, "Admin not found"));

        if (admin.getCompanyEmail() != null) {
            throw new ConflictException(AdminErrorCode.COMPANY_EMAIL_ALREADY_GENERATED, "Admin already has a company email");
        }

        String destinationEmail = getDestinationEmailForRole(admin.getRole());
        String firstName = admin.getFullName().split(" ")[0].toLowerCase();
        
        // This is a simplified alias generation. In production, we'd handle collisions (e.g. damilola1@)
        String aliasEmail = firstName + "@atlaspay.name.ng";

        // Call external port
        cloudflareEmailPort.createEmailRoutingRule(aliasEmail, destinationEmail);

        // Update Admin
        admin.assignCompanyEmail(new EmailAddress(aliasEmail));
        
        // Register event for Auth module to update secondaryIdentifier
        
        adminRepository.save(admin);
        publishEvents(admin, eventPublisher);

        return new GenerateEmailResult(aliasEmail);
    }

    private String getDestinationEmailForRole(AdminRole role) {
        return switch (role) {
            case MASTER -> "dotuniyanda@atlaspay.name.ng";
            case SUPPORT -> "support@atlaspay.name.ng";
            case BILLING -> "billing@atlaspay.name.ng";
            case CAREERS -> "careers@atlaspay.name.ng";
            case GENERAL -> "hello@atlaspay.name.ng";
        };
    }
}

