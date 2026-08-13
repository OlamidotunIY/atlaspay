package com.atlaspay.simulator.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name = "simulator_accounts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SimulatorAccountJpaEntity {
    @Id
    private String id;
    
    @Column(nullable = false, length = 100)
    private String reference;
    
    @Column(nullable = false, length = 50)
    private String bankName;
    
    @Column(nullable = false, length = 3)
    private String bankCode;
    
    @Column(nullable = false, unique = true)
    private long accountSerial;
    
    @Column(length = 10)
    private String nuban;
    
    @Column(nullable = false, length = 100)
    private String accountName;
    
    @Column(nullable = false, length = 255)
    private String callbackUrl;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    @Column(nullable = false)
    private ZonedDateTime createdAt;
}
