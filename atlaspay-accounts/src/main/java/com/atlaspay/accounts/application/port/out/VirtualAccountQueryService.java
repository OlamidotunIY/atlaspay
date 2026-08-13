package com.atlaspay.accounts.application.port.out;

public interface VirtualAccountQueryService {
    long countByOwnerId(String ownerId);
    boolean existsByOwnerIdAndBankName(String ownerId, String bankName);
}
