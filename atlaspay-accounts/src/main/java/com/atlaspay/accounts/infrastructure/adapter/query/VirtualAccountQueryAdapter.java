package com.atlaspay.accounts.infrastructure.adapter.query;

import com.atlaspay.accounts.application.port.out.VirtualAccountQueryService;
import com.atlaspay.accounts.infrastructure.repository.JpaVirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VirtualAccountQueryAdapter implements VirtualAccountQueryService {
    
    private final JpaVirtualAccountRepository repository;
    
    @Override
    public long countByOwnerId(String ownerId) {
        return repository.findByOwnerId(ownerId).size();
    }
    
    @Override
    public boolean existsByOwnerIdAndBankName(String ownerId, String bankName) {
        return repository.findByOwnerId(ownerId).stream()
                .anyMatch(account -> account.getBankName().equalsIgnoreCase(bankName));
    }
}
