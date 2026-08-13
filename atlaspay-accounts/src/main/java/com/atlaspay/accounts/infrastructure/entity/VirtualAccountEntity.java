package com.atlaspay.accounts.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "virtual_accounts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VirtualAccountEntity {

    @Id
    @Column(length = 50)
    private String id;
    
    @Column(nullable = false, length = 50)
    private String ownerId;
    
    @Column(nullable = false, length = 20)
    private String ownerType;
    
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
