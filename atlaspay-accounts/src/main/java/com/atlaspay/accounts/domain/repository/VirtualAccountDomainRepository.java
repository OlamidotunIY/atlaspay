package com.atlaspay.accounts.domain.repository;

import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.shared.domain.id.VirtualAccountId;
import com.atlaspay.shared.domain.valueobject.NUBAN;
import com.atlaspay.shared.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface VirtualAccountDomainRepository extends Repository<VirtualAccount, VirtualAccountId> {
    Optional<VirtualAccount> findByNuban(NUBAN nuban);
    List<VirtualAccount> findByOwnerId(String ownerId);
    boolean existsByNuban(NUBAN nuban);
}
