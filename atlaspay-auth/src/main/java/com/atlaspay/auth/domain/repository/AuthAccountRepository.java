package com.atlaspay.auth.domain.repository;

import com.atlaspay.auth.domain.model.AuthAccount;
import com.atlaspay.auth.domain.model.PrincipalType;

import java.util.Optional;

public interface AuthAccountRepository {
    Long nextIdentity();
    AuthAccount save(AuthAccount authAccount);
    Optional<AuthAccount> findById(Long id);
    Optional<AuthAccount> findByPrincipalIdAndType(Long principalId, PrincipalType type);
    boolean existsByPrincipalIdAndType(Long principalId, PrincipalType type);
}
