package com.atlaspay.identity.infrastructure.adapter.persistence;

import com.atlaspay.identity.domain.model.SubAccount;
import com.atlaspay.identity.domain.repository.SubAccountRepository;
import com.atlaspay.identity.infrastructure.entity.SubAccountJpaEntity;
import com.atlaspay.identity.infrastructure.mapper.SubAccountMapper;
import com.atlaspay.identity.infrastructure.repository.SpringDataSubAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SubAccountRepositoryAdapter implements SubAccountRepository {

    private final SpringDataSubAccountRepository jpaRepository;
    private final com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator;
    private final SubAccountMapper mapper;

    public SubAccountRepositoryAdapter(SpringDataSubAccountRepository jpaRepository, com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator, SubAccountMapper mapper) {
        this.sequenceGenerator = sequenceGenerator;
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SubAccount save(SubAccount subAccount) {
        SubAccountJpaEntity entity = mapper.toEntity(subAccount);
        jpaRepository.save(entity);
        return subAccount;
    }

    @Override
    public Optional<SubAccount> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SubAccount> findByMerchantIdAndBankCodeAndAccountNumber(Long merchantId, String bankCode, String accountNumber) {
        return jpaRepository.findByIntegrationAndBankCodeAndAccountNumber(merchantId, bankCode, accountNumber).map(mapper::toDomain);
    }


    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("subaccount_seq");
    }


}
