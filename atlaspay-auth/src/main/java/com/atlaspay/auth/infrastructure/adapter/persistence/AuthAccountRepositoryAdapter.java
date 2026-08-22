package com.atlaspay.auth.infrastructure.adapter.persistence;

import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.model.PrincipalType;
import com.atlaspay.auth.domain.repository.AuthAccountRepository;
import com.atlaspay.auth.infrastructure.entity.AuthAccountJpaEntity;
import com.atlaspay.auth.infrastructure.mapper.AuthAccountMapper;
import com.atlaspay.auth.infrastructure.repository.SpringDataAuthAccountRepository;
import com.atlaspay.shared.infrastructure.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AuthAccountRepositoryAdapter implements AuthAccountRepository {

    private final SpringDataAuthAccountRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final AuthAccountMapper mapper;

    public AuthAccountRepositoryAdapter(SpringDataAuthAccountRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, AuthAccountMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("auth_account_seq");
    }

    @Override
    public AuthAccount save(AuthAccount account) {
        AuthAccountJpaEntity entity = mapper.toEntity(account);
        jpaRepository.save(entity);
        return account;
    }

    @Override
    public Optional<AuthAccount> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByPrincipalIdAndType(Long principalId, PrincipalType type) {
        return jpaRepository.findByPrincipalIdAndPrincipalType(principalId, type).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPrincipalIdAndType(Long principalId, PrincipalType type) {
        return jpaRepository.existsByPrincipalIdAndPrincipalType(principalId, type);
    }
}
