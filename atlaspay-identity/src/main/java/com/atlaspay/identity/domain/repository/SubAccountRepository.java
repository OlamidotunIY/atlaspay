package com.atlaspay.identity.domain.repository;

import com.atlaspay.identity.domain.model.SubAccount;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.id.SubAccountId;

import java.util.Optional;

public interface SubAccountRepository {
    SubAccount save(SubAccount subAccount);
    Optional<SubAccount> findById(SubAccountId id);
    Optional<SubAccount> findByMerchantIdAndBankCodeAndAccountNumber(MerchantId merchantId, String bankCode, String accountNumber);
}
