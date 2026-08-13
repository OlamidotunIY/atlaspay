package com.atlaspay.identity.infrastructure.adapter.persistence;

import com.atlaspay.identity.domain.model.SubAccount;
import com.atlaspay.identity.domain.repository.SubAccountRepository;
import com.atlaspay.identity.infrastructure.entity.SubAccountJpaEntity;
import com.atlaspay.identity.infrastructure.repository.SpringDataSubAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SubAccountRepositoryAdapter implements SubAccountRepository {

    private final SpringDataSubAccountRepository jpaRepository;
    private final com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator;

    public SubAccountRepositoryAdapter(SpringDataSubAccountRepository jpaRepository, com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SubAccount save(SubAccount subAccount) {
        SubAccountJpaEntity entity = toEntity(subAccount);
        jpaRepository.save(entity);
        return subAccount;
    }

    @Override
    public Optional<SubAccount> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<SubAccount> findByMerchantIdAndBankCodeAndAccountNumber(Long merchantId, String bankCode, String accountNumber) {
        return jpaRepository.findByIntegrationAndBankCodeAndAccountNumber(merchantId, bankCode, accountNumber).map(this::toDomain);
    }


    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("subaccount_seq");
    }

    private SubAccountJpaEntity toEntity(SubAccount domain) {
        SubAccountJpaEntity entity = new SubAccountJpaEntity();
        entity.setId(domain.getId());
        entity.setIntegration(domain.getMerchantId());
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
                entity.getId(),
                entity.getIntegration(),
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
