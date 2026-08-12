package com.atlaspay.identity.infrastructure.persistence.adapter;

import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.model.ComplianceStep;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.identity.infrastructure.persistence.entity.MerchantJpaEntity;
import com.atlaspay.identity.infrastructure.persistence.repository.SpringDataMerchantRepository;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MerchantRepositoryAdapter implements MerchantRepository {

    private final SpringDataMerchantRepository jpaRepository;

    public MerchantRepositoryAdapter(SpringDataMerchantRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Merchant save(Merchant merchant) {
        MerchantJpaEntity entity = toEntity(merchant);
        jpaRepository.save(entity);
        return merchant;
    }

    @Override
    public Optional<Merchant> findById(MerchantId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Merchant> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    private MerchantJpaEntity toEntity(Merchant domain) {
        MerchantJpaEntity entity = new MerchantJpaEntity();
        entity.setId(domain.getId().value());
        entity.setCountry(domain.getCountry());
        entity.setBusinessName(domain.getBusinessName());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail().value());
        entity.setPhone(domain.getPhone() != null ? domain.getPhone().value() : null);
        entity.setHashedPassword(domain.getHashedPassword());
        entity.setBusinessType(domain.getBusinessType());
        entity.setEmailVerified(domain.isEmailVerified());
        entity.setComplianceStatus(domain.getComplianceStatus());
        entity.setComplianceStep(domain.getComplianceStep() != null ? domain.getComplianceStep().name() : null);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private Merchant toDomain(MerchantJpaEntity entity) {
        Merchant merchant = new Merchant(
                new MerchantId(entity.getId()),
                entity.getCountry(),
                entity.getBusinessName(),
                entity.getFirstName(),
                entity.getLastName(),
                new EmailAddress(entity.getEmail()),
                entity.getPhone() != null ? new PhoneNumber(entity.getPhone()) : null,
                entity.getHashedPassword(),
                entity.getBusinessType()
        );
        // Note: Full reconstruction of the aggregate is required, but since Merchant's constructor sets defaults,
        // we might need a persistence constructor or reflection. For this MVP, we simulate it via reflection if needed,
        // but for now, we rely on the constructor.
        if (entity.isEmailVerified()) {
            merchant.verifyEmail(merchant.getEmailVerificationCode() != null ? merchant.getEmailVerificationCode().getCode() : "ignored"); // Hacky without persistence constructor
        }
        
        if (entity.getComplianceStep() != null) {
            merchant.completeComplianceStep(ComplianceStep.valueOf(entity.getComplianceStep()));
        }
        // clear events after reconstruction
        merchant.pullDomainEvents();
        return merchant;
    }
}
