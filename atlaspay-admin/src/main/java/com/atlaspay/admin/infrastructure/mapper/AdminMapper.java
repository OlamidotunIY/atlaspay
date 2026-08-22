package com.atlaspay.admin.infrastructure.mapper;

import com.atlaspay.admin.domain.model.Admin;
import com.atlaspay.admin.infrastructure.entity.AdminJpaEntity;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public Admin toDomain(AdminJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        EmailAddress companyEmail = entity.getCompanyEmail() != null ? new EmailAddress(entity.getCompanyEmail()) : null;

        return new Admin(
            entity.getId(),
            entity.getEmployeeCode(),
            entity.getFullName(),
            new EmailAddress(entity.getPersonalEmail()),
            companyEmail,
            entity.getRole(),
            entity.getStatus()
        );
    }

    public AdminJpaEntity toEntity(Admin domain) {
        if (domain == null) {
            return null;
        }

        AdminJpaEntity entity = new AdminJpaEntity();
        entity.setId(domain.getId());
        entity.setEmployeeCode(domain.getEmployeeCode());
        entity.setFullName(domain.getFullName());
        entity.setPersonalEmail(domain.getPersonalEmail().value());
        
        if (domain.getCompanyEmail() != null) {
            entity.setCompanyEmail(domain.getCompanyEmail().value());
        }
        
        entity.setRole(domain.getRole());
        entity.setStatus(domain.getStatus());
        
        return entity;
    }
}
