package com.atlaspay.accounts.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "virtual_accounts", 
    indexes = {
        @Index(name = "idx_va_integration", columnList = "integration"),
        @Index(name = "idx_va_customer", columnList = "customer_code"),
        @Index(name = "idx_va_nuban", columnList = "nuban")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_va_nuban", columnNames = {"nuban"}),
        @UniqueConstraint(name = "uk_va_idempotency", columnNames = {"idempotencyKey"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VirtualAccountEntity {

    @Id
    private Long id;
    
    @Column(nullable = false)
    private Long integration;
    
    @Column(name = "customer_code", nullable = false, length = 50)
    private String customerCode;
    
    @Column(nullable = false, length = 100)
    private String accountName;
    
    @Column(nullable = false, length = 50)
    private String bankName;
    
    @Column(length = 10, unique = true)
    private String nuban;
    
    @Setter
    @Column(nullable = false, length = 30)
    private String status;
    
    @Column(length = 100, unique = true)
    private String idempotencyKey;
    
    @Setter
    @Version
    @Column(nullable = false)
    private Integer version;
    
    @Column(nullable = false)
    private ZonedDateTime createdAt;
    
    @Setter
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    public VirtualAccountEntity(Long id, Long integration, String customerCode, String accountName, String bankName, String nuban, String status, String idempotencyKey, Integer version, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.integration = integration;
        this.customerCode = customerCode;
        this.accountName = accountName;
        this.bankName = bankName;
        this.nuban = nuban;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
