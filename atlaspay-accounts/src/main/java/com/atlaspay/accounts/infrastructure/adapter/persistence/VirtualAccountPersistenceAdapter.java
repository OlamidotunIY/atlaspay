package com.atlaspay.accounts.infrastructure.adapter.persistence;

import com.atlaspay.accounts.domain.model.AccountStatus;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.accounts.infrastructure.entity.VirtualAccountEntity;
import com.atlaspay.accounts.infrastructure.repository.JpaVirtualAccountRepository;
import com.atlaspay.accounts.infrastructure.mapper.VirtualAccountMapper;
import com.atlaspay.shared.domain.valueobject.NUBAN;
import com.atlaspay.shared.infrastructure.DomainSequenceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VirtualAccountPersistenceAdapter implements VirtualAccountDomainRepository {

    private final JpaVirtualAccountRepository repository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final VirtualAccountMapper mapper;

    @Override
    public VirtualAccount save(VirtualAccount account) {
        VirtualAccountEntity entity = mapper.toEntity(account);
        VirtualAccountEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<VirtualAccount> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<VirtualAccount> findByNuban(NUBAN nuban) {
        return repository.findByNuban(nuban.value()).map(mapper::toDomain);
    }

    @Override
    public List<VirtualAccount> findByIntegration(Long integration) {
        return repository.findByIntegration(integration).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNuban(NUBAN nuban) {
        return repository.existsByNuban(nuban.value());
    }


    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("virtual_account_seq");
    }

}
