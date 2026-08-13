package com.atlaspay.accounts.infrastructure.adapter.persistence;

import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.model.AccountStatus;
import com.atlaspay.accounts.domain.model.OwnerType;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.accounts.infrastructure.entity.VirtualAccountEntity;
import com.atlaspay.accounts.infrastructure.repository.JpaVirtualAccountRepository;
import com.atlaspay.shared.domain.id.VirtualAccountId;
import com.atlaspay.shared.domain.valueobject.NUBAN;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VirtualAccountPersistenceAdapter implements VirtualAccountDomainRepository {

    private final JpaVirtualAccountRepository repository;

    @Override
    public VirtualAccount save(VirtualAccount account) {
        VirtualAccountEntity entity = toEntity(account);
        entity.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        VirtualAccountEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<VirtualAccount> findById(VirtualAccountId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<VirtualAccount> findByNuban(NUBAN nuban) {
        return repository.findByNuban(nuban.value()).map(this::toDomain);
    }

    @Override
    public List<VirtualAccount> findByOwnerId(String ownerId) {
        return repository.findByOwnerId(ownerId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNuban(NUBAN nuban) {
        return repository.existsByNuban(nuban.value());
    }

    private VirtualAccountEntity toEntity(VirtualAccount domain) {
        VirtualAccountEntity entity = repository.findById(domain.getId().value())
                .orElse(new VirtualAccountEntity(
                        domain.getId().value(),
                        domain.getOwnerId(),
                        domain.getOwnerType().name(),
                        domain.getAccountName(),
                        domain.getBankName(),
                        null,
                        domain.getStatus().name(),
                        domain.getIdempotencyKey(),
                        0,
                        ZonedDateTime.now(ZoneOffset.UTC),
                        ZonedDateTime.now(ZoneOffset.UTC)
                ));
        
        entity.setStatus(domain.getStatus().name());
        entity.setNuban(domain.getNuban() != null ? domain.getNuban().value() : null);
        
        return entity;
    }

    private VirtualAccount toDomain(VirtualAccountEntity entity) {
        return new VirtualAccount(
                new VirtualAccountId(entity.getId()),
                entity.getOwnerId(),
                entity.getAccountName(),
                OwnerType.valueOf(entity.getOwnerType()),
                entity.getBankName(),
                entity.getIdempotencyKey(),
                AccountStatus.valueOf(entity.getStatus()),
                entity.getNuban() != null ? new NUBAN(entity.getNuban()) : null
        );
    }
}
