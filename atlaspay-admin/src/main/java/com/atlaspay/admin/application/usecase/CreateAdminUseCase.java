package com.atlaspay.admin.application.usecase;

import com.atlaspay.admin.application.command.CreateAdminCommand;
import com.atlaspay.admin.application.dto.CreateAdminResult;
import com.atlaspay.admin.domain.exception.AdminErrorCode;
import com.atlaspay.admin.domain.model.Admin;
import com.atlaspay.admin.domain.model.AdminRole;
import com.atlaspay.admin.domain.repository.AdminRepository;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.ConflictException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateAdminUseCase extends BaseUseCase<CreateAdminCommand, CreateAdminResult> {

    private final AdminRepository adminRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateAdminUseCase(AdminRepository adminRepository, DomainEventPublisher eventPublisher) {
        this.adminRepository = adminRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public CreateAdminResult execute(CreateAdminCommand command) {
        if (adminRepository.existsByPersonalEmail(command.personalEmail())) {
            throw new ConflictException(AdminErrorCode.ADMIN_EMAIL_ALREADY_EXISTS, "Admin with personal email already exists");
        }

        // Generate employee code (e.g., AP- + 6 alphanumeric chars)
        String employeeCode = "AP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        AdminRole role = AdminRole.valueOf(command.role().toUpperCase());

        Admin admin = Admin.create(
            adminRepository.nextIdentity(),
            employeeCode,
            command.fullName(),
            new EmailAddress(command.personalEmail()),
            role
        );

        adminRepository.save(admin);
        publishEvents(admin, eventPublisher);

        return new CreateAdminResult(admin.getId(), admin.getEmployeeCode());
    }
}
