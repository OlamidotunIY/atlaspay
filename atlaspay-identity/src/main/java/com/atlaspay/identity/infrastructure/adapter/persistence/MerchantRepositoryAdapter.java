package com.atlaspay.identity.infrastructure.adapter.persistence;

import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.identity.infrastructure.entity.MerchantJpaEntity;
import com.atlaspay.identity.infrastructure.mapper.MerchantMapper;
import com.atlaspay.identity.infrastructure.repository.SpringDataMerchantRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MerchantRepositoryAdapter implements MerchantRepository {

    private final SpringDataMerchantRepository jpaRepository;
    private final com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator;
    private final MerchantMapper mapper;

    public MerchantRepositoryAdapter(SpringDataMerchantRepository jpaRepository, com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator, MerchantMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Merchant save(Merchant merchant) {
        MerchantJpaEntity entity = mapper.toEntity(merchant);
        jpaRepository.save(entity);
        return merchant;
    }

    @Override
    public Optional<Merchant> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Merchant> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("merchant_seq");
    }


}
