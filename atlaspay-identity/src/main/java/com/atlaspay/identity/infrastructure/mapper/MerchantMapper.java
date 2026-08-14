package com.atlaspay.identity.infrastructure.mapper;

import com.atlaspay.identity.domain.model.ComplianceStatus;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.model.ComplianceStep;
import com.atlaspay.identity.infrastructure.entity.MerchantJpaEntity;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {

    public MerchantJpaEntity toEntity(Merchant domain) {
        if (domain == null) return null;

        return new MerchantJpaEntity(
                domain.getId(),
                domain.getCountry(),
                domain.getBusinessName(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getEmail().value(),
                domain.getPhone() != null ? domain.getPhone().value() : null,
                domain.getHashedPassword(),
                domain.getBusinessType(),
                domain.isEmailVerified(),
                domain.getComplianceStatus(),
                domain.getComplianceStep() != null ? domain.getComplianceStep().name() : null,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public Merchant toDomain(MerchantJpaEntity entity) {
        if (entity == null) return null;

        return new Merchant(
                entity.getId(),
                entity.getCountry(),
                entity.getBusinessName(),
                entity.getFirstName(),
                entity.getLastName(),
                new EmailAddress(entity.getEmail()),
                entity.getPhone() != null ? new PhoneNumber(entity.getPhone()) : null,
                entity.getHashedPassword(),
                entity.getBusinessType(),
                entity.isEmailVerified(),
                entity.getComplianceStatus() != null ? ComplianceStatus.valueOf(entity.getComplianceStatus().name()) : ComplianceStatus.NOT_STARTED,
                entity.getComplianceStep() != null ? ComplianceStep.valueOf(entity.getComplianceStep()) : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
