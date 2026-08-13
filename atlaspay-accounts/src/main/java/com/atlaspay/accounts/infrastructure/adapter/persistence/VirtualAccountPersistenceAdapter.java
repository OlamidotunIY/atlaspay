package com.atlaspay.accounts.infrastructure.adapter.persistence;

import com.atlaspay.accounts.domain.model.AccountStatus;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.accounts.infrastructure.entity.VirtualAccountEntity;
import com.atlaspay.accounts.infrastructure.repository.JpaVirtualAccountRepository;
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

    @Override
    public VirtualAccount save(VirtualAccount account) {
        VirtualAccountEntity entity = toEntity(account);
        VirtualAccountEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<VirtualAccount> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
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
        return repository.findByNuban(nuban.value()).map(this::toDomain);
    }

    @Override
    public List<VirtualAccount> findByIntegration(Long integration) {
        return repository.findByIntegration(integration).stream()
                .map(this::toDomain)
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

    private VirtualAccountEntity toEntity(VirtualAccount domain) {
        return VirtualAccountEntity.builder()
                .id(domain.getId())
                .integration(domain.getIntegration())
                .customerCode(domain.getCustomerCode())
                .accountName(domain.getAccountName())
                .bankName(domain.getBankName())
                .nuban(domain.getNuban() != null ? domain.getNuban().value() : null)
                .status(domain.getStatus().name())
                .idempotencyKey(domain.getIdempotencyKey())
                .build();
    }

    private VirtualAccount toDomain(VirtualAccountEntity entity) {
        VirtualAccount account = new VirtualAccount(
                entity.getId(),
                entity.getIntegration(),
                entity.getCustomerCode(),
                entity.getAccountName(),
                entity.getBankName(),
                entity.getIdempotencyKey(),
                AccountStatus.valueOf(entity.getStatus()),
                entity.getNuban() != null ? new NUBAN(entity.getNuban()) : null
        );
        account.pullDomainEvents();
        return account;
    }
}
