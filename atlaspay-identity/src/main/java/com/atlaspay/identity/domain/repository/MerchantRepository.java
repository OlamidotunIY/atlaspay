package com.atlaspay.identity.domain.repository;

import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.shared.domain.id.MerchantId;

import java.util.Optional;

public interface MerchantRepository {
    Merchant save(Merchant merchant);
    Optional<Merchant> findById(MerchantId id);
    Optional<Merchant> findByEmail(String email);
}
