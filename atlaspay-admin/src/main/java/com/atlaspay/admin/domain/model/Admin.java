package com.atlaspay.admin.domain.model;

import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.admin.domain.event.AdminCreated;
import com.atlaspay.admin.domain.event.AdminCompanyEmailGenerated;

public class Admin extends AggregateRoot<Long> {
    
    private Long id;
    private final String employeeCode;
    private final String fullName;
    private final EmailAddress personalEmail;
    private EmailAddress companyEmail;
    private final AdminRole role;
    private AdminStatus status;
    
    public Admin(Long id, String employeeCode, String fullName, EmailAddress personalEmail, EmailAddress companyEmail, AdminRole role, AdminStatus status) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.personalEmail = personalEmail;
        this.companyEmail = companyEmail;
        this.role = role;
        this.status = status;
    }
    
    public static Admin create(Long id, String employeeCode, String fullName, EmailAddress personalEmail, AdminRole role) {
        Admin admin = new Admin(id, employeeCode, fullName, personalEmail, null, role, AdminStatus.ACTIVE);
        admin.registerEvent(new AdminCreated(id, employeeCode, fullName, personalEmail.value(), role.name()));
        return admin;
    }
    
    public void assignCompanyEmail(EmailAddress companyEmail) {
        this.companyEmail = companyEmail;
        this.registerEvent(new AdminCompanyEmailGenerated(this.id, this.employeeCode, companyEmail.value()));
    }
    
    public void suspend() {
        this.status = AdminStatus.SUSPENDED;
    }
    
    public void activate() {
        this.status = AdminStatus.ACTIVE;
    }

    @Override
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public EmailAddress getPersonalEmail() {
        return personalEmail;
    }

    public EmailAddress getCompanyEmail() {
        return companyEmail;
    }

    public AdminRole getRole() {
        return role;
    }

    public AdminStatus getStatus() {
        return status;
    }
}
