package com.atlaspay.auth.infrastructure.entity;

import com.atlaspay.auth.domain.model.VerificationStatus;
import com.atlaspay.auth.domain.model.VerificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "verifications", indexes = {
        @Index(name = "idx_verification_identifier", columnList = "identifier"),
        @Index(name = "idx_verification_type_value_status", columnList = "type, value, status")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VerificationJpaEntity {
    @Id
    private Long id;

    private Long authAccountId;

    @Column(nullable = false)
    private String identifier;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status;
    
    @Column(nullable = false)
    private int attempts;
    
    @Column(nullable = false)
    private int maxAttempts;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime expiresAt;
    
    private ZonedDateTime verifiedAt;
}

