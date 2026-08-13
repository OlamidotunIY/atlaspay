package com.atlaspay.identity.domain.repository;

import com.atlaspay.identity.domain.model.SubAccount;

import java.util.Optional;

public interface SubAccountRepository {
    Long nextIdentity();
    SubAccount save(SubAccount subAccount);
    Optional<SubAccount> findById(Long id);
    Optional<SubAccount> findByMerchantIdAndBankCodeAndAccountNumber(Long merchantId, String bankCode, String accountNumber);
}
