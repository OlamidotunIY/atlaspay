package com.atlaspay.identity.infrastructure.adapter.persistence;

import com.atlaspay.identity.domain.model.SubAccount;
import com.atlaspay.identity.domain.repository.SubAccountRepository;
import com.atlaspay.identity.infrastructure.entity.SubAccountJpaEntity;
import com.atlaspay.identity.infrastructure.repository.SpringDataSubAccountRepository;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.id.SubAccountId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SubAccountRepositoryAdapter implements SubAccountRepository {

    private final SpringDataSubAccountRepository jpaRepository;

    public SubAccountRepositoryAdapter(SpringDataSubAccountRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SubAccount save(SubAccount subAccount) {
        SubAccountJpaEntity entity = toEntity(subAccount);
        jpaRepository.save(entity);
        return subAccount;
    }

    @Override
    public Optional<SubAccount> findById(SubAccountId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<SubAccount> findByMerchantIdAndBankCodeAndAccountNumber(MerchantId merchantId, String bankCode, String accountNumber) {
        return jpaRepository.findByMerchantIdAndBankCodeAndAccountNumber(merchantId.value(), bankCode, accountNumber).map(this::toDomain);
    }

    private SubAccountJpaEntity toEntity(SubAccount domain) {
        SubAccountJpaEntity entity = new SubAccountJpaEntity();
        entity.setId(domain.getId().value());
        entity.setMerchantId(domain.getMerchantId().value());
        entity.setBankCode(domain.getBankCode());
        entity.setAccountNumber(domain.getAccountNumber());
        entity.setAccountName(domain.getAccountName());
        entity.setDescription(domain.getDescription());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private SubAccount toDomain(SubAccountJpaEntity entity) {
        SubAccount subAccount = new SubAccount(
                new SubAccountId(entity.getId()),
                new MerchantId(entity.getMerchantId()),
                entity.getBankCode(),
                entity.getAccountNumber(),
                entity.getAccountName(),
                entity.getDescription()
        );
        if (!entity.isActive()) {
            subAccount.deactivate();
        }
        subAccount.pullDomainEvents();
        return subAccount;
    }
}
