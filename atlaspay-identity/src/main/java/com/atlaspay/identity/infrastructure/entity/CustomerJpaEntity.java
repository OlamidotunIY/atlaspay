package com.atlaspay.identity.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "customers", 
    indexes = {
        @Index(name = "idx_customer_integration", columnList = "integration"),
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_code", columnList = "code")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_integration_email", columnNames = {"integration", "email"})
    }
)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "integration", nullable = false)
    private Long integration;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Setter
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
