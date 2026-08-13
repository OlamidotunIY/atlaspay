package com.atlaspay.accounts.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@Builder
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class VirtualAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    
    @Column(nullable = false, length = 30)
    private String status;
    
    @Column(length = 100, unique = true)
    private String idempotencyKey;
    
    @Version
    @Column(nullable = false)
    private Integer version;
    
    @Column(nullable = false)
    private ZonedDateTime createdAt;
    
    @Column(nullable = false)
    private ZonedDateTime updatedAt;
}
